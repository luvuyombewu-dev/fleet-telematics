package za.co.fleettelematics.dto.request;

import jakarta.validation.constraints.NotNull;

public class DriverAssignmentRequest {

    @NotNull
    private Long driverId;

    @NotNull
    private Long vehicleId;

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }
}
