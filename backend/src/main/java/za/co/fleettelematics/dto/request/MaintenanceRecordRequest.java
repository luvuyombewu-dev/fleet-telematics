package za.co.fleettelematics.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public class MaintenanceRecordRequest {

    @NotNull
    @Positive
    private Long vehicleId;

    @NotBlank
    private String maintenanceType;

    private String description;
    private LocalDate scheduledDate;
    private LocalDate completedDate;

    @DecimalMin("0.0")
    private BigDecimal serviceMileage;

    @DecimalMin("0.0")
    private BigDecimal cost;

    private String status;
    private String notes;

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
}
