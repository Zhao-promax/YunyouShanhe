package com.yunyou.shanhe.ticket.service;

import com.yunyou.shanhe.common.core.BizException;
import com.yunyou.shanhe.ticket.channel.ChannelLockResult;
import com.yunyou.shanhe.ticket.dto.BookRequest;
import com.yunyou.shanhe.ticket.entity.Channel;
import com.yunyou.shanhe.ticket.entity.TicketOrder;
import com.yunyou.shanhe.ticket.entity.WaitlistOrder;
import com.yunyou.shanhe.ticket.enums.OrderStatus;
import com.yunyou.shanhe.ticket.enums.WaitlistStatus;
import com.yunyou.shanhe.ticket.mapper.WaitlistOrderMapper;
import com.yunyou.shanhe.ticket.mq.EventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private ChannelService channelService;
    @Mock
    private OrderService orderService;
    @Mock
    private WaitlistService waitlistService;
    @Mock
    private WaitlistOrderMapper waitlistMapper;
    @Mock
    private EventPublisher eventPublisher;
    @Mock
    private TicketTypeService ticketTypeService;

    @InjectMocks
    private BookingService bookingService;

    private Channel channel() {
        Channel c = new Channel();
        c.setId(1L);
        c.setScenicId(100L);
        return c;
    }

    private BookRequest request() {
        return new BookRequest(100L, LocalDate.of(2026, 11, 22), "09:00-12:00", "adult", 2);
    }

    @Test
    void shouldComputeAmountAndCreatePendingOrder() {
        when(channelService.getByScenicId(100L)).thenReturn(channel());
        when(channelService.lockTicketWithLog(any(), any())).thenReturn(ChannelLockResult.success("ch-1"));
        when(ticketTypeService.getPrice(100L, "adult")).thenReturn(6000L);

        TicketOrder order = bookingService.bookSpot(1L, request());

        assertEquals(12000L, order.getAmount());
        assertEquals(OrderStatus.PENDING_PAY.getCode(), order.getStatus());
        assertEquals(1, order.getBizType());
        assertEquals(2, order.getQuantity());
        verify(orderService).create(order);
    }

    @Test
    void shouldThrowWhenLockFails() {
        when(channelService.getByScenicId(100L)).thenReturn(channel());
        when(channelService.lockTicketWithLog(any(), any())).thenReturn(ChannelLockResult.fail("无库存"));

        assertThrows(BizException.class, () -> bookingService.bookSpot(1L, request()));
    }

    @Test
    void shouldThrowWhenConfirmPaymentOnWrongStatus() {
        TicketOrder issued = new TicketOrder();
        issued.setStatus(OrderStatus.ISSUED.getCode());
        when(orderService.getByOrderNo("SO1")).thenReturn(issued);

        assertThrows(BizException.class, () -> bookingService.confirmPayment("SO1"));
    }

    @Test
    void shouldThrowWhenRefundNotOwner() {
        TicketOrder order = new TicketOrder();
        order.setUserId(999L);
        order.setStatus(OrderStatus.ISSUED.getCode());
        when(orderService.getByOrderNo("SO1")).thenReturn(order);

        assertThrows(BizException.class, () -> bookingService.refundTicket(1L, "SO1"));
    }

    @Test
    void shouldKeepQueueHeadWhenConsumeLockFails() {
        WaitlistOrder w = new WaitlistOrder();
        w.setId(10L);
        w.setPreferredSlot("09:00-12:00");
        w.setStatus(WaitlistStatus.QUEUING.getCode());
        when(waitlistService.popTop(100L, LocalDate.of(2026, 11, 22), "09:00-12:00", 1)).thenReturn(List.of(10L));
        when(waitlistMapper.selectById(10L)).thenReturn(w);
        when(channelService.getByScenicId(100L)).thenReturn(channel());
        when(channelService.lockTicketWithLog(any(), any())).thenReturn(ChannelLockResult.fail("被抢占"));
        when(waitlistMapper.update(isNull(), any())).thenReturn(1);

        int issued = bookingService.consumeWaitlist(100L, LocalDate.of(2026, 11, 22), "09:00-12:00", 1);

        assertEquals(0, issued);
        verify(waitlistService, never()).remove(any(), any(), any(), any());
    }

    @Test
    void shouldSkipWhenAlreadyClaimed() {
        when(waitlistService.popTop(100L, LocalDate.of(2026, 11, 22), "09:00-12:00", 1)).thenReturn(List.of(10L));
        when(waitlistMapper.update(isNull(), any())).thenReturn(0);

        int issued = bookingService.consumeWaitlist(100L, LocalDate.of(2026, 11, 22), "09:00-12:00", 1);

        assertEquals(0, issued);
        verify(waitlistService).remove(100L, LocalDate.of(2026, 11, 22), "09:00-12:00", 10L);
        verify(channelService, never()).lockTicketWithLog(any(), any());
    }

    @Test
    void shouldMarkWonWhenConsumeLockSucceeds() {
        WaitlistOrder w = new WaitlistOrder();
        w.setId(10L);
        w.setUserId(1L);
        w.setScenicId(100L);
        w.setPreferredSlot("09:00-12:00");
        w.setStatus(WaitlistStatus.QUEUING.getCode());
        when(waitlistService.popTop(100L, LocalDate.of(2026, 11, 22), "09:00-12:00", 1)).thenReturn(List.of(10L));
        when(waitlistMapper.selectById(10L)).thenReturn(w);
        when(channelService.getByScenicId(100L)).thenReturn(channel());
        when(channelService.lockTicketWithLog(any(), any())).thenReturn(ChannelLockResult.success("ch-1"));
        when(waitlistMapper.update(isNull(), any())).thenReturn(1);

        int issued = bookingService.consumeWaitlist(100L, LocalDate.of(2026, 11, 22), "09:00-12:00", 1);

        assertEquals(1, issued);
        verify(waitlistService).remove(100L, LocalDate.of(2026, 11, 22), "09:00-12:00", 10L);
        verify(orderService).create(any(TicketOrder.class));
    }
}
