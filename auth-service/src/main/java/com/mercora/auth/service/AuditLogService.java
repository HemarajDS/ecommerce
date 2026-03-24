package com.mercora.auth.service;

import com.mercora.auth.model.AuditAction;
import com.mercora.auth.model.AuditLog;
import com.mercora.auth.model.UserAccount;
import com.mercora.auth.repository.AuditLogRepository;
import java.time.Instant;
import java.util.Map;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Async("authTaskExecutor")
    public void record(UserAccount user, AuditAction action, Map<String, Object> metadata) {
        AuditLog log = new AuditLog();
        log.setUserId(user != null ? user.getId() : null);
        log.setEmail(user != null ? user.getEmail() : null);
        log.setAction(action);
        log.setCreatedAt(Instant.now());
        log.setMetadata(metadata);
        auditLogRepository.save(log);
    }

    @Async("authTaskExecutor")
    public void record(String email, AuditAction action, Map<String, Object> metadata) {
        AuditLog log = new AuditLog();
        log.setEmail(email);
        log.setAction(action);
        log.setCreatedAt(Instant.now());
        log.setMetadata(metadata);
        auditLogRepository.save(log);
    }
}
