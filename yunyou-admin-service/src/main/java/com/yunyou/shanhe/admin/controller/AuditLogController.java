package com.yunyou.shanhe.admin.controller;

import com.yunyou.shanhe.admin.entity.AuditLog;
import com.yunyou.shanhe.admin.service.AuditLogService;
import com.yunyou.shanhe.common.core.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/audit")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping("/list")
    public Result<List<AuditLog>> list() {
        return Result.ok(auditLogService.list());
    }
}
