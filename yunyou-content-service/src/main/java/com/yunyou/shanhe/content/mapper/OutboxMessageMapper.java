package com.yunyou.shanhe.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunyou.shanhe.content.entity.OutboxMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OutboxMessageMapper extends BaseMapper<OutboxMessage> {
}
