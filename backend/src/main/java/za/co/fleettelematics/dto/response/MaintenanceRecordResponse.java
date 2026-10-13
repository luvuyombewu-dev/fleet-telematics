package za.co.fleettelematics.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public class MaintenanceRecordResponse {

    private Long id;
    private Long vehicleId;
    private String maintenanceType;
    private String description;
    private LocalDate scheduledDate;
    private LocalDate completedDate;
    private BigDecimal serviceMileage;
    private BigDecimal cost;
    private String status;
    private String notes;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }
    public String getMaintenanceType() { return maintenanceType; }
    public void setMaintenanceType(String maintenanceType) { this.maintenanceType = maintenanceType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(LocalDate scheduledDate) { this.scheduledDate = scheduledDate; }
    public LocalDate getCompletedDate() { return completedDate; }
    public void setCompletedDate(LocalDate completedDate) { this.completedDate = completedDate; }
    public BigDecimal getServiceMileage() { return serviceMileage; }
    public void setServiceMileage(BigDecimal serviceMileage) { this.serviceMileage = serviceMileage; }
    public BigDecimal getCost() { return cost; }
    public void setCost(BigDecimal cost) { this.cost = cost; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
