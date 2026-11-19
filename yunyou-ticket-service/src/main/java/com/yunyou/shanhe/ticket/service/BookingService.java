package com.yunyou.shanhe.ticket.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yunyou.shanhe.common.core.BizException;
import com.yunyou.shanhe.common.event.NotifyEvent;
import com.yunyou.shanhe.common.event.OrderChangedEvent;
import com.yunyou.shanhe.common.event.RefundEvent;
import com.yunyou.shanhe.common.event.TicketIssuedEvent;
import com.yunyou.shanhe.ticket.channel.ChannelLockRequest;
import com.yunyou.shanhe.ticket.channel.ChannelLockResult;
import com.yunyou.shanhe.ticket.dto.BookRequest;
import com.yunyou.shanhe.ticket.dto.WaitlistRequest;
import com.yunyou.shanhe.ticket.entity.Channel;
import com.yunyou.shanhe.ticket.entity.TicketOrder;
import com.yunyou.shanhe.ticket.entity.WaitlistOrder;
import com.yunyou.shanhe.ticket.enums.OrderStatus;
import com.yunyou.shanhe.ticket.enums.WaitlistStatus;
import com.yunyou.shanhe.ticket.mapper.WaitlistOrderMapper;
import com.yunyou.shanhe.ticket.mq.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * 票务核心业务：现货下单、候补登记、候补消费、退票、支付确认、超时关闭、候补熔断。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final ChannelService channelService;
    private final OrderService orderService;
    private final WaitlistService waitlistService;
    private final WaitlistOrderMapper waitlistMapper;
    private final EventPublisher eventPublisher;
    private final TicketTypeService ticketTypeService;

    /**
     * 现货下单：先锁后付（景区锁票 → 生成待支付订单）。
     */
    public TicketOrder bookSpot(Long userId, BookRequest request) {
        Channel channel = channelService.getByScenicId(request.scenicId());

        String requestId = UUID.randomUUID().toString().replace("-", "");
        ChannelLockResult lockResult = channelService.lockTicketWithLog(channel, new ChannelLockRequest(
                requestId, request.scenicId(), request.visitDate(), request.timeSlot(),
                request.ticketType(), request.quantity()));
        if (!lockResult.success()) {
            throw new BizException(lockResult.message());
        }

        long now = System.currentTimeMillis();
        TicketOrder order = new TicketOrder();
        order.setOrderNo(genNo("SO"));
        order.setBizType(1);
        order.setUserId(userId);
        order.setScenicId(request.scenicId());
        order.setVisitDate(request.visitDate());
        order.setTimeSlot(request.timeSlot());
        order.setTicketType(request.ticketType());
        order.setQuantity(request.quantity());
        Long unitPrice = ticketTypeService.getPrice(request.scenicId(), request.ticketType());
        order.setAmount(unitPrice * request.quantity());
        order.setStatus(OrderStatus.PENDING_PAY.getCode());
        order.setChannelId(channel.getId());
        order.setRequestId(requestId);
        order.setExpireTime(now + 15 * 60 * 1000L);
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        orderService.create(order);
        return order;
    }

    /**
     * 现货支付确认（开发期 Mock；生产由支付服务回调触发）。待支付 → 已出票。
     */
    public void confirmPayment(String orderNo) {
        TicketOrder order = orderService.getByOrderNo(orderNo);
        if (order == null) {
            throw new BizException("订单不存在");
        }
        if (order.getStatus() != OrderStatus.PENDING_PAY.getCode()) {
            throw new BizException("订单状态不允许支付确认");
        }
        orderService.updateStatus(orderNo, OrderStatus.PENDING_PAY.getCode(), OrderStatus.ISSUED.getCode(),
                "支付成功", "pay");
        eventPublisher.publishIssued(new TicketIssuedEvent(orderNo, order.getUserId(), order.getScenicId(), order.getAmount()));
        eventPublisher.publishOrderChanged(new OrderChangedEvent(orderNo, OrderStatus.PENDING_PAY.getCode(),
                OrderStatus.ISSUED.getCode(), "支付成功"));
        eventPublisher.publishNotify(new NotifyEvent(order.getUserId(), "ticket_issued", "预约成功"));
    }

    /**
     * 退票：已出票 → 已退票，释放库存由景区 Webhook 流入候补队列。
     */
    public void refundTicket(Long userId, String orderNo) {
        TicketOrder order = orderService.getByOrderNo(orderNo);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BizException("订单不存在");
        }
        if (order.getStatus() != OrderStatus.ISSUED.getCode()) {
            throw new BizException("订单状态不允许退票");
        }
        Channel channel = channelService.getByScenicId(order.getScenicId());
        // TODO 调用景区退票接口并核验；当前 Mock 仅释放锁票（requestId 暂代 channelOrderId）
        channelService.releaseTicketWithLog(channel, order.getRequestId());

        orderService.updateStatus(orderNo, OrderStatus.ISSUED.getCode(), OrderStatus.REFUNDED.getCode(),
                "用户退票", "user");
        eventPublisher.publishRefund(new RefundEvent(orderNo, order.getPayOrderNo(), userId, order.getAmount(), "用户退票"));
        eventPublisher.publishNotify(new NotifyEvent(userId, "ticket_refunded", "退票成功"));
    }

    /**
     * 现货订单超时关闭（供定时任务调用）。
     */
    public void closeExpiredOrder(String orderNo) {
        TicketOrder order = orderService.getByOrderNo(orderNo);
        if (order == null || order.getStatus() != OrderStatus.PENDING_PAY.getCode()) {
            return;
        }
        if (order.getExpireTime() != null && order.getExpireTime() > System.currentTimeMillis()) {
            return;
        }
        Channel channel = channelService.getByScenicId(order.getScenicId());
        channelService.releaseTicketWithLog(channel, order.getRequestId());
        orderService.updateStatus(orderNo, OrderStatus.PENDING_PAY.getCode(), OrderStatus.CLOSED.getCode(),
                "支付超时关闭", "system");
        eventPublisher.publishOrderChanged(new OrderChangedEvent(orderNo, OrderStatus.PENDING_PAY.getCode(),
                OrderStatus.CLOSED.getCode(), "支付超时关闭"));
    }

    /**
     * 候补登记：先付后退（支付成功后入队排队）。
     */
    public WaitlistOrder registerWaitlist(Long userId, WaitlistRequest request) {
        long now = System.currentTimeMillis();
        WaitlistOrder waitlist = new WaitlistOrder();
        waitlist.setWaitlistNo(genNo("WL"));
        waitlist.setUserId(userId);
        waitlist.setScenicId(request.scenicId());
        waitlist.setVisitDate(request.visitDate());
        waitlist.setPreferredSlot(request.preferredSlot());
        waitlist.setShiftSlots(request.shiftSlots());
        waitlist.setStatus(WaitlistStatus.QUEUING.getCode());
        waitlist.setEnqueueTime(now);
        // TODO 支付服务生成支付单号（先付后退）
        waitlist.setPayOrderNo("PAY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        waitlist.setCreatedAt(now);
        waitlist.setUpdatedAt(now);
        waitlistMapper.insert(waitlist);

        waitlistService.enqueue(request.scenicId(), request.visitDate(), request.preferredSlot(), waitlist.getId(), now);
        return waitlist;
    }

    /**
     * 候补消费：库存释放 K 个名额，取队首 K 条依次尝试预约。
     */
    public int consumeWaitlist(Long scenicId, LocalDate visitDate, String slot, int k) {
        List<Long> ids = waitlistService.popTop(scenicId, visitDate, slot, k);
        int issued = 0;
        for (Long waitlistId : ids) {
            WaitlistOrder w = waitlistMapper.selectById(waitlistId);
            if (w == null || w.getStatus() != WaitlistStatus.QUEUING.getCode()) {
                waitlistService.remove(scenicId, visitDate, slot, waitlistId);
                continue;
            }
            Channel channel = channelService.getByScenicId(scenicId);
            String requestId = UUID.randomUUID().toString().replace("-", "");
            ChannelLockResult lockResult = channelService.lockTicketWithLog(channel, new ChannelLockRequest(
                    requestId, scenicId, visitDate, slot, w.getPreferredSlot(), 1));
            if (lockResult.success()) {
                markWon(w, lockResult.channelOrderId());
                waitlistService.remove(scenicId, visitDate, slot, waitlistId);
                issued++;
            } else {
                // 临时失败（名额被抢占）：保留队首继续等待下一批名额
                log.info("候补临时失败，保留队首 waitlistNo={}", w.getWaitlistNo());
            }
        }
        return issued;
    }

    /**
     * 取消候补：出队 + 状态更新 + 触发退款事件。
     */
    public void cancelWaitlist(Long userId, String waitlistNo) {
        WaitlistOrder w = waitlistMapper.selectOne(
                Wrappers.<WaitlistOrder>lambdaQuery().eq(WaitlistOrder::getWaitlistNo, waitlistNo));
        if (w == null || !w.getUserId().equals(userId)) {
            throw new BizException("候补单不存在");
        }
        if (w.getStatus() != WaitlistStatus.QUEUING.getCode()) {
            throw new BizException("候补单状态不允许取消");
        }
        long now = System.currentTimeMillis();
        WaitlistOrder update = new WaitlistOrder();
        update.setId(w.getId());
        update.setStatus(WaitlistStatus.CANCELLED.getCode());
        update.setUpdatedAt(now);
        waitlistMapper.updateById(update);
        waitlistService.remove(w.getScenicId(), w.getVisitDate(), w.getPreferredSlot(), w.getId());

        eventPublisher.publishRefund(new RefundEvent(w.getWaitlistNo(), w.getPayOrderNo(), userId, 0L, "用户取消候补"));
        eventPublisher.publishNotify(new NotifyEvent(userId, "waitlist_cancelled", "候补已取消"));
    }

    /**
     * 候补熔断：截止时间未中签的候补批量终止并退款（供定时任务调用）。
     */
    public int cutoffWaitlists(Long scenicId, LocalDate visitDate, String slot) {
        List<WaitlistOrder> queuing = waitlistMapper.selectList(Wrappers.<WaitlistOrder>lambdaQuery()
                .eq(WaitlistOrder::getScenicId, scenicId)
                .eq(WaitlistOrder::getVisitDate, visitDate)
                .eq(WaitlistOrder::getPreferredSlot, slot)
                .eq(WaitlistOrder::getStatus, WaitlistStatus.QUEUING.getCode()));
        for (WaitlistOrder w : queuing) {
            long now = System.currentTimeMillis();
            WaitlistOrder update = new WaitlistOrder();
            update.setId(w.getId());
            update.setStatus(WaitlistStatus.CUTOFF_FAILED.getCode());
            update.setUpdatedAt(now);
            waitlistMapper.updateById(update);
            waitlistService.remove(scenicId, visitDate, slot, w.getId());

            eventPublisher.publishRefund(new RefundEvent(w.getWaitlistNo(), w.getPayOrderNo(), w.getUserId(), 0L, "候补熔断截止"));
            eventPublisher.publishNotify(new NotifyEvent(w.getUserId(), "waitlist_failed", "候补未中签，已退款"));
        }
        return queuing.size();
    }

    private void markWon(WaitlistOrder w, String channelOrderId) {
        long now = System.currentTimeMillis();
        TicketOrder order = new TicketOrder();
        order.setOrderNo(genNo("WO"));
        order.setBizType(2);
        order.setUserId(w.getUserId());
        order.setScenicId(w.getScenicId());
        order.setVisitDate(w.getVisitDate());
        order.setTimeSlot(w.getPreferredSlot());
        order.setTicketType(""); // TODO 票种
        order.setQuantity(1);
        order.setAmount(0L); // TODO 价格
        order.setStatus(OrderStatus.ISSUED.getCode());
        order.setRequestId(UUID.randomUUID().toString().replace("-", ""));
        order.setPayOrderNo(w.getPayOrderNo());
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        orderService.create(order);

        WaitlistOrder update = new WaitlistOrder();
        update.setId(w.getId());
        update.setStatus(WaitlistStatus.WON.getCode());
        update.setOrderId(order.getId());
        update.setUpdatedAt(now);
        waitlistMapper.updateById(update);

        eventPublisher.publishIssued(new TicketIssuedEvent(order.getOrderNo(), w.getUserId(), w.getScenicId(), order.getAmount()));
        eventPublisher.publishNotify(new NotifyEvent(w.getUserId(), "waitlist_won", "候补中签成功"));
    }

    private String genNo(String prefix) {
        return prefix + System.currentTimeMillis() + (int) (Math.random() * 10000);
    }
}
