package za.co.fleettelematics.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class AlertResponse {

    private Long id;
    private Long vehicleId;
    private Long driverId;
    private String alertType;
    private String severity;
    private String description;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String status;
    private OffsetDateTime createdAt;
    private OffsetDateTime acknowledgedAt;
    private OffsetDateTime resolvedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }
    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }
    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getAcknowledgedAt() { return acknowledgedAt; }
    public void setAcknowledgedAt(OffsetDateTime acknowledgedAt) { this.acknowledgedAt = acknowledgedAt; }
    public OffsetDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(OffsetDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
