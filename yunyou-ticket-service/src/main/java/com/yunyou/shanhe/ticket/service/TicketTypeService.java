package com.yunyou.shanhe.ticket.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yunyou.shanhe.common.core.BizException;
import com.yunyou.shanhe.ticket.entity.TicketType;
import com.yunyou.shanhe.ticket.mapper.TicketTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 票种价格服务。
 */
@Service
@RequiredArgsConstructor
public class TicketTypeService {

    private final TicketTypeMapper ticketTypeMapper;

    public Long getPrice(Long scenicId, String typeCode) {
        TicketType type = ticketTypeMapper.selectOne(Wrappers.<TicketType>lambdaQuery()
                .eq(TicketType::getScenicId, scenicId)
                .eq(TicketType::getTypeCode, typeCode)
                .eq(TicketType::getStatus, 1));
        if (type == null) {
            throw new BizException("票种未配置或已停售");
        }
        return type.getPrice();
    }

    public List<TicketType> listByScenic(Long scenicId) {
        return ticketTypeMapper.selectList(Wrappers.<TicketType>lambdaQuery()
                .eq(scenicId != null, TicketType::getScenicId, scenicId)
                .orderByAsc(TicketType::getId));
    }

    public TicketType save(TicketType type) {
        long now = System.currentTimeMillis();
        if (type.getId() == null) {
            type.setCreatedAt(now);
            type.setUpdatedAt(now);
            ticketTypeMapper.insert(type);
        } else {
            type.setUpdatedAt(now);
            ticketTypeMapper.updateById(type);
        }
        return type;
    }
}
