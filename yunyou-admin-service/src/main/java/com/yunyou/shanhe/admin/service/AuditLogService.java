package com.yunyou.shanhe.admin.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yunyou.shanhe.admin.entity.AuditLog;
import com.yunyou.shanhe.admin.mapper.AuditLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 审计日志（敏感操作不可篡改留存）。
 */
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogMapper auditLogMapper;

    public void record(Long operatorId, String action, String targetType, String targetId, String detail) {
        long now = System.currentTimeMillis();
        AuditLog log = new AuditLog();
        log.setOperatorId(operatorId);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetail(detail);
        log.setCreatedAt(now);
        auditLogMapper.insert(log);
    }

    public List<AuditLog> list() {
        return auditLogMapper.selectList(Wrappers.<AuditLog>lambdaQuery()
                .orderByDesc(AuditLog::getId));
    }
}
