package za.co.fleettelematics.dto.request;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class VehicleRequest {
    @NotBlank private String registrationNumber;
    private String fleetNumber;
    private String vin;
    @NotBlank private String make;
    @NotBlank private String model;
    @Min(1900) @Max(2100) private Integer vehicleYear;
    @NotBlank private String vehicleType;
    @NotBlank private String fuelType;
    @DecimalMin("0.0") private BigDecimal mileage;
    private String status;
    private String deviceId;
    private String trackingStatus;

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
    public String getFleetNumber() { return fleetNumber; }
    public void setFleetNumber(String fleetNumber) { this.fleetNumber = fleetNumber; }
    public String getVin() { return vin; }
    public void setVin(String vin) { this.vin = vin; }
    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public Integer getVehicleYear() { return vehicleYear; }
    public void setVehicleYear(Integer vehicleYear) { this.vehicleYear = vehicleYear; }
    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
    public String getFuelType() { return fuelType; }
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }
    public BigDecimal getMileage() { return mileage; }
    public void setMileage(BigDecimal mileage) { this.mileage = mileage; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getTrackingStatus() { return trackingStatus; }
    public void setTrackingStatus(String trackingStatus) { this.trackingStatus = trackingStatus; }
}
