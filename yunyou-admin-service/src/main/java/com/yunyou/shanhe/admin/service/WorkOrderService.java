package com.yunyou.shanhe.admin.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yunyou.shanhe.admin.dto.WorkOrderHandleRequest;
import com.yunyou.shanhe.admin.entity.WorkOrder;
import com.yunyou.shanhe.admin.mapper.WorkOrderMapper;
import com.yunyou.shanhe.common.core.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 售后工单服务。
 */
@Service
@RequiredArgsConstructor
public class WorkOrderService {

    private final WorkOrderMapper workOrderMapper;
    private final AuditLogService auditLogService;

    public List<WorkOrder> list(Integer status) {
        return workOrderMapper.selectList(Wrappers.<WorkOrder>lambdaQuery()
                .eq(status != null, WorkOrder::getStatus, status)
                .orderByDesc(WorkOrder::getId));
    }

    public void handle(Long id, WorkOrderHandleRequest request) {
        WorkOrder order = workOrderMapper.selectById(id);
        if (order == null) {
            throw new BizException("工单不存在");
        }
        long now = System.currentTimeMillis();
        WorkOrder update = new WorkOrder();
        update.setId(id);
        update.setStatus(request.status());
        update.setHandlerId(request.handlerId());
        update.setRemark(request.remark());
        update.setUpdatedAt(now);
        workOrderMapper.updateById(update);

        auditLogService.record(request.handlerId(), "work_order_handle", "work_order",
                String.valueOf(id), request.remark());
    }
}
