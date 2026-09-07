package com.fintrack.audit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintrack.audit.entity.AuditAction;
import com.fintrack.audit.entity.AuditLog;
import com.fintrack.audit.repository.AuditLogRepository;
import com.fintrack.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Reusable audit yazma noktası — spec'in istediği üç eylem grubu
 * (TransactionService, BudgetService, SubscriptionService) bu tek servisi
 * çağırır, JSON serileştirme ve entity kurma mantığını tekrarlamaz.
 * <p>
 * Kasıtlı olarak {@code REQUIRES_NEW} DEĞİL — audit kaydı, denetlediği
 * işlemle aynı transaction'da olmalı: ana işlem rollback olursa audit kaydı
 * da olmamalı (aksi halde "gerçekleşmemiş" bir işlem için audit trail'de
 * kayıt görünür, ki bu audit'in bütünlüğünü bozar).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void record(UUID userId, AuditAction action, String entityType, UUID entityId, Object oldValue, Object newValue) {
        AuditLog log = AuditLog.builder()
                .user(userId != null ? userRepository.getReferenceById(userId) : null)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .oldValue(toJson(oldValue))
                .newValue(toJson(newValue))
                .build();
        auditLogRepository.save(log);
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            log.warn("Audit değeri JSON'a çevrilemedi: {}", value, ex);
            return null;
        }
    }
}
