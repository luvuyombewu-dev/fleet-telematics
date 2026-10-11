package za.co.fleettelematics.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class TripRequest {

    @NotNull
    @Positive
    private Long vehicleId;

    @Positive
    private Long driverId;

    @NotNull
    private OffsetDateTime startTime;

    private OffsetDateTime endTime;

    private String startLocation;

    private String destination;

    @DecimalMin("0.0")
    private BigDecimal distanceKm;

    @DecimalMin("0.0")
    private BigDecimal averageSpeedKmh;

    @DecimalMin("0.0")
    private BigDecimal maximumSpeedKmh;

    @PositiveOrZero
    private Long durationSeconds;

    @Size(max = 30)
    private String status;

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public OffsetDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(OffsetDateTime startTime) {
        this.startTime = startTime;
    }

    public OffsetDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(OffsetDateTime endTime) {
        this.endTime = endTime;
    }

    public String getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(String startLocation) {
        this.startLocation = startLocation;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public BigDecimal getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(BigDecimal distanceKm) {
        this.distanceKm = distanceKm;
    }

    public BigDecimal getAverageSpeedKmh() {
        return averageSpeedKmh;
    }

    public void setAverageSpeedKmh(BigDecimal averageSpeedKmh) {
        this.averageSpeedKmh = averageSpeedKmh;
    }

    public BigDecimal getMaximumSpeedKmh() {
        return maximumSpeedKmh;
    }

    public void setMaximumSpeedKmh(BigDecimal maximumSpeedKmh) {
        this.maximumSpeedKmh = maximumSpeedKmh;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
