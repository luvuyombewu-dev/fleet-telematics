package za.co.fleettelematics.controller;

import java.util.List;
import org.springframework.web.bind.annotation.*;
import za.co.fleettelematics.dto.response.AuditLogResponse;
import za.co.fleettelematics.service.AuditLogService;

@RestController
@RequestMapping("/api/v1/audit-logs")
public class AuditLogController {
    private final AuditLogService service;
    public AuditLogController(AuditLogService service) { this.service = service; }
    @GetMapping public List<AuditLogResponse> findAll(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long entityId) {
        return service.findAll(userId, entityType, entityId);
    }
}
