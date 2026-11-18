package com.yunyou.shanhe.admin.controller;

import com.yunyou.shanhe.admin.dto.WorkOrderHandleRequest;
import com.yunyou.shanhe.admin.entity.WorkOrder;
import com.yunyou.shanhe.admin.service.WorkOrderService;
import com.yunyou.shanhe.common.core.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/work-order")
@RequiredArgsConstructor
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    @GetMapping("/list")
    public Result<List<WorkOrder>> list(@RequestParam(required = false) Integer status) {
        return Result.ok(workOrderService.list(status));
    }

    @PostMapping("/{id}/handle")
    public Result<Void> handle(@PathVariable Long id, @Valid @RequestBody WorkOrderHandleRequest request) {
        workOrderService.handle(id, request);
        return Result.ok();
    }
}
