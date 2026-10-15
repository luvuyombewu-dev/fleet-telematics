package za.co.fleettelematics.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import za.co.fleettelematics.dto.request.TelemetryRequest;
import za.co.fleettelematics.dto.response.TelemetryResponse;
import za.co.fleettelematics.entity.Telemetry;
import za.co.fleettelematics.entity.Vehicle;
import za.co.fleettelematics.exception.ResourceNotFoundException;
import za.co.fleettelematics.repository.TelemetryRepository;
import za.co.fleettelematics.repository.VehicleRepository;

@Service
@Transactional
public class TelemetryService {
    private final TelemetryRepository telemetryRepository;
    private final VehicleRepository vehicleRepository;

    public TelemetryService(TelemetryRepository telemetryRepository, VehicleRepository vehicleRepository) {
        this.telemetryRepository = telemetryRepository;
        this.vehicleRepository = vehicleRepository;
    }

    public TelemetryResponse record(TelemetryRequest request) {
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", request.getVehicleId()));
        Telemetry telemetry = new Telemetry();
        telemetry.setVehicle(vehicle);
        telemetry.setLatitude(request.getLatitude());
        telemetry.setLongitude(request.getLongitude());
        telemetry.setSpeedKmh(request.getSpeedKmh());
        telemetry.setHeading(request.getHeading());
        telemetry.setMileage(request.getMileage());
        telemetry.setFuelLevelPercent(request.getFuelLevelPercent());
        telemetry.setEngineTemperatureC(request.getEngineTemperatureC());
        telemetry.setBatteryVoltage(request.getBatteryVoltage());
        telemetry.setIgnitionStatus(Boolean.TRUE.equals(request.getIgnitionStatus()));
        telemetry.setEngineStatus(Boolean.TRUE.equals(request.getEngineStatus()));
        telemetry.setRecordedAt(request.getRecordedAt());
        if (request.getMileage() != null && request.getMileage().compareTo(vehicle.getMileage()) > 0) {
            vehicle.setMileage(request.getMileage());
            vehicleRepository.save(vehicle);
        }
        return toResponse(telemetryRepository.save(telemetry));
    }

    @Transactional(readOnly = true)
    public List<TelemetryResponse> findRecentByVehicle(Long vehicleId) {
        requireVehicle(vehicleId);
        return telemetryRepository.findTop100ByVehicleIdOrderByRecordedAtDesc(vehicleId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public TelemetryResponse findLatestByVehicle(Long vehicleId) {
        return findRecentByVehicle(vehicleId).stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Telemetry for vehicle", vehicleId));
    }

    private void requireVehicle(Long id) {
        if (!vehicleRepository.existsById(id)) throw new ResourceNotFoundException("Vehicle", id);
    }

    private TelemetryResponse toResponse(Telemetry telemetry) {
        TelemetryResponse response = new TelemetryResponse();
        response.setId(telemetry.getId());
        response.setVehicleId(telemetry.getVehicle().getId());
        response.setLatitude(telemetry.getLatitude());
        response.setLongitude(telemetry.getLongitude());
        response.setSpeedKmh(telemetry.getSpeedKmh());
        response.setHeading(telemetry.getHeading());
        response.setMileage(telemetry.getMileage());
        response.setFuelLevelPercent(telemetry.getFuelLevelPercent());
        response.setEngineTemperatureC(telemetry.getEngineTemperatureC());
        response.setBatteryVoltage(telemetry.getBatteryVoltage());
        response.setIgnitionStatus(telemetry.isIgnitionStatus());
        response.setEngineStatus(telemetry.isEngineStatus());
        response.setRecordedAt(telemetry.getRecordedAt());
        return response;
    }
}
