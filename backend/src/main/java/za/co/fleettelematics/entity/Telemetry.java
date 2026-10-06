package za.co.fleettelematics.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "telemetry")
public class Telemetry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(nullable = false)
    private BigDecimal latitude;

    @Column(nullable = false)
    private BigDecimal longitude;

    @Column(name = "speed_kmh", nullable = false)
    private BigDecimal speedKmh = BigDecimal.ZERO;

    private BigDecimal heading;

    private BigDecimal mileage;

    @Column(name = "fuel_level_percent")
    private BigDecimal fuelLevelPercent;

    @Column(name = "engine_temperature_c")
    private BigDecimal engineTemperatureC;

    @Column(name = "battery_voltage")
    private BigDecimal batteryVoltage;

    @Column(name = "ignition_status", nullable = false)
    private boolean ignitionStatus = false;

    @Column(name = "engine_status", nullable = false)
    private boolean engineStatus = false;

    @Column(name = "recorded_at", nullable = false)
    private OffsetDateTime recordedAt;

    protected Telemetry() {
    }

    public Long getId() {
        return id;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
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

    public boolean isIgnitionStatus() {
        return ignitionStatus;
    }

    public void setIgnitionStatus(boolean ignitionStatus) {
        this.ignitionStatus = ignitionStatus;
    }

    public boolean isEngineStatus() {
        return engineStatus;
    }

    public void setEngineStatus(boolean engineStatus) {
        this.engineStatus = engineStatus;
    }

    public OffsetDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(OffsetDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }
}
