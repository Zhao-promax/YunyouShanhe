package com.yunyou.shanhe.ticket.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yunyou.shanhe.ticket.entity.OrderStatusLog;
import com.yunyou.shanhe.ticket.entity.TicketOrder;
import com.yunyou.shanhe.ticket.mapper.OrderStatusLogMapper;
import com.yunyou.shanhe.ticket.mapper.TicketOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 订单服务：订单创建、查询、状态单向流转 + 状态变更日志。
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final TicketOrderMapper orderMapper;
    private final OrderStatusLogMapper statusLogMapper;

    public void create(TicketOrder order) {
        orderMapper.insert(order);
    }

    public TicketOrder getByOrderNo(String orderNo) {
        return orderMapper.selectOne(
                Wrappers.<TicketOrder>lambdaQuery().eq(TicketOrder::getOrderNo, orderNo));
    }

    public void updateStatus(String orderNo, Integer fromStatus, Integer toStatus, String reason, String operator) {
        TicketOrder order = getByOrderNo(orderNo);
        if (order == null) {
            return;
        }
        TicketOrder update = new TicketOrder();
        update.setId(order.getId());
        update.setStatus(toStatus);
        update.setUpdatedAt(System.currentTimeMillis());
        orderMapper.updateById(update);

        long now = System.currentTimeMillis();
        OrderStatusLog log = new OrderStatusLog();
        log.setOrderNo(orderNo);
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        log.setReason(reason);
        log.setOperator(operator);
        log.setCreatedAt(now);
        statusLogMapper.insert(log);
    }
}
