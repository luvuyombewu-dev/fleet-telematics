package za.co.fleettelematics.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class TelemetryResponse {

    private Long id;
    private Long vehicleId;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal speedKmh;
    private BigDecimal heading;
    private BigDecimal mileage;
    private BigDecimal fuelLevelPercent;
    private BigDecimal engineTemperatureC;
    private BigDecimal batteryVoltage;
    private Boolean ignitionStatus;
    private Boolean engineStatus;
    private OffsetDateTime recordedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getSpeedKmh() {
        return speedKmh;
    }

    public void setSpeedKmh(BigDecimal speedKmh) {
        this.speedKmh = speedKmh;
    }

    public BigDecimal getHeading() {
        return heading;
    }

    public void setHeading(BigDecimal heading) {
        this.heading = heading;
    }

    public BigDecimal getMileage() {
        return mileage;
    }

    public void setMileage(BigDecimal mileage) {
        this.mileage = mileage;
    }

    public BigDecimal getFuelLevelPercent() {
        return fuelLevelPercent;
    }

    public void setFuelLevelPercent(BigDecimal fuelLevelPercent) {
        this.fuelLevelPercent = fuelLevelPercent;
    }

    public BigDecimal getEngineTemperatureC() {
        return engineTemperatureC;
    }

    public void setEngineTemperatureC(BigDecimal engineTemperatureC) {
        this.engineTemperatureC = engineTemperatureC;
    }

    public BigDecimal getBatteryVoltage() {
        return batteryVoltage;
    }

    public void setBatteryVoltage(BigDecimal batteryVoltage) {
        this.batteryVoltage = batteryVoltage;
    }

    public Boolean getIgnitionStatus() {
        return ignitionStatus;
    }

    public void setIgnitionStatus(Boolean ignitionStatus) {
        this.ignitionStatus = ignitionStatus;
    }

    public Boolean getEngineStatus() {
        return engineStatus;
    }

    public void setEngineStatus(Boolean engineStatus) {
        this.engineStatus = engineStatus;
    }

    public OffsetDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(OffsetDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }
}
