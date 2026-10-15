package za.co.fleettelematics.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import za.co.fleettelematics.dto.response.AuditLogResponse;
import za.co.fleettelematics.entity.AuditLog;
import za.co.fleettelematics.repository.AuditLogRepository;

@Service
@Transactional(readOnly = true)
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public List<AuditLogResponse> findAll(Long userId, String entityType, Long entityId) {
        return auditLogRepository.findAll().stream()
                .filter(log -> userId == null || (log.getUser() != null && log.getUser().getId().equals(userId)))
                .filter(log -> entityType == null || entityType.equalsIgnoreCase(log.getEntityType()))
                .filter(log -> entityId == null || entityId.equals(log.getEntityId()))
                .map(this::toResponse).toList();
    }

    private AuditLogResponse toResponse(AuditLog log) {
        AuditLogResponse response = new AuditLogResponse();
        response.setId(log.getId());
        response.setUserId(log.getUser() == null ? null : log.getUser().getId());
        response.setAction(log.getAction());
        response.setEntityType(log.getEntityType());
        response.setEntityId(log.getEntityId());
        response.setMetadata(log.getMetadata());
        response.setCreatedAt(log.getCreatedAt());
        return response;
    }
}
