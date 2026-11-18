package com.yunyou.shanhe.pay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 资金流水台账。
 */
@Data
@TableName("fund_ledger")
public class FundLedger {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String ledgerNo;

    private Long userId;

    private String payOrderNo;

    private Integer bizType;

    private Long amount;

    private Long balanceAfter;

    private Long createdAt;
}
