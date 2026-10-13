package za.co.fleettelematics.dto.response;

import java.time.OffsetDateTime;
import tools.jackson.databind.JsonNode;

public class AuditLogResponse {

    private Long id;
    private Long userId;
    private String action;
    private String entityType;
    private Long entityId;
    private JsonNode metadata;
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }
    public JsonNode getMetadata() { return metadata; }
    public void setMetadata(JsonNode metadata) { this.metadata = metadata; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
