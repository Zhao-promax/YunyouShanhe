package com.yunyou.shanhe.ticket.service;

import com.yunyou.shanhe.common.core.BizException;
import com.yunyou.shanhe.ticket.entity.TicketType;
import com.yunyou.shanhe.ticket.mapper.TicketTypeMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketTypeServiceTest {

    @Mock
    private TicketTypeMapper ticketTypeMapper;

    @InjectMocks
    private TicketTypeService ticketTypeService;

    @Test
    void shouldReturnPriceWhenTypeExists() {
        TicketType type = new TicketType();
        type.setPrice(6000L);
        when(ticketTypeMapper.selectOne(any())).thenReturn(type);

        assertEquals(6000L, ticketTypeService.getPrice(1L, "adult"));
    }

    @Test
    void shouldThrowWhenTypeNotFound() {
        when(ticketTypeMapper.selectOne(any())).thenReturn(null);

        assertThrows(BizException.class, () -> ticketTypeService.getPrice(1L, "adult"));
    }
}
