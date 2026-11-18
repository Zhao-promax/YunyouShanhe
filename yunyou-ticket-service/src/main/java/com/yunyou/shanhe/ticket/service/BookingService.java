package com.yunyou.shanhe.ticket.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yunyou.shanhe.common.core.BizException;
import com.yunyou.shanhe.ticket.channel.ChannelLockRequest;
import com.yunyou.shanhe.ticket.channel.ChannelLockResult;
import com.yunyou.shanhe.ticket.channel.ScenicChannelClient;
import com.yunyou.shanhe.ticket.dto.BookRequest;
import com.yunyou.shanhe.ticket.dto.WaitlistRequest;
import com.yunyou.shanhe.ticket.entity.Channel;
import com.yunyou.shanhe.ticket.entity.TicketOrder;
import com.yunyou.shanhe.ticket.entity.WaitlistOrder;
import com.yunyou.shanhe.ticket.enums.OrderStatus;
import com.yunyou.shanhe.ticket.enums.WaitlistStatus;
import com.yunyou.shanhe.ticket.mapper.WaitlistOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * 票务核心业务：现货下单、候补登记、候补消费、取消候补。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final ChannelService channelService;
    private final OrderService orderService;
    private final WaitlistService waitlistService;
    private final WaitlistOrderMapper waitlistMapper;

    /**
     * 现货下单：先锁后付（景区锁票 → 生成待支付订单）。
     */
    public TicketOrder bookSpot(Long userId, BookRequest request) {
        Channel channel = channelService.getByScenicId(request.scenicId());
        ScenicChannelClient client = channelService.getClient(channel);

        String requestId = UUID.randomUUID().toString().replace("-", "");
        ChannelLockResult lockResult = client.lockTicket(new ChannelLockRequest(
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
        order.setAmount(0L); // TODO 从票种价格配置计算
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
            ScenicChannelClient client = channelService.getClient(channel);
            String requestId = UUID.randomUUID().toString().replace("-", "");
            ChannelLockResult lockResult = client.lockTicket(new ChannelLockRequest(
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
        // TODO 发送退款事件（RabbitMQ），由支付服务原路退款
    }

    private void markWon(WaitlistOrder w, String channelOrderId) {
        long now = System.currentTimeMillis();
        WaitlistOrder update = new WaitlistOrder();
        update.setId(w.getId());
        update.setStatus(WaitlistStatus.WON.getCode());
        update.setUpdatedAt(now);
        waitlistMapper.updateById(update);
        // TODO 生成已出票订单 + 发送候补成功事件（通知，无需再扣款）
    }

    private String genNo(String prefix) {
        return prefix + System.currentTimeMillis() + (int) (Math.random() * 10000);
    }
}
