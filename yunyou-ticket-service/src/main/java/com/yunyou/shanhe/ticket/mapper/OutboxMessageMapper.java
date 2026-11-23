package com.yunyou.shanhe.ticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunyou.shanhe.ticket.entity.OutboxMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OutboxMessageMapper extends BaseMapper<OutboxMessage> {
}
