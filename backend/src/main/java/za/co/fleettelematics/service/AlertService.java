package za.co.fleettelematics.service;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import za.co.fleettelematics.dto.request.AlertRequest;
import za.co.fleettelematics.dto.response.AlertResponse;
import za.co.fleettelematics.entity.Alert;
import za.co.fleettelematics.entity.Driver;
import za.co.fleettelematics.entity.Vehicle;
import za.co.fleettelematics.exception.ResourceNotFoundException;
import za.co.fleettelematics.repository.AlertRepository;
import za.co.fleettelematics.repository.DriverRepository;
import za.co.fleettelematics.repository.VehicleRepository;

@Service
@Transactional
public class AlertService {
    private final AlertRepository alertRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;

    public AlertService(AlertRepository alertRepository, VehicleRepository vehicleRepository,
                        DriverRepository driverRepository) {
        this.alertRepository = alertRepository;
        this.vehicleRepository = vehicleRepository;
        this.driverRepository = driverRepository;
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> findAll(Long vehicleId, Long driverId, String status, String severity) {
        return alertRepository.findAll().stream()
                .filter(alert -> vehicleId == null || alert.getVehicle().getId().equals(vehicleId))
                .filter(alert -> driverId == null || (alert.getDriver() != null && alert.getDriver().getId().equals(driverId)))
                .filter(alert -> status == null || status.equalsIgnoreCase(alert.getStatus()))
                .filter(alert -> severity == null || severity.equalsIgnoreCase(alert.getSeverity()))
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AlertResponse findById(Long id) { return toResponse(getEntity(id)); }

    public AlertResponse create(AlertRequest request) {
        Alert alert = new Alert();
        apply(alert, request);
        return toResponse(alertRepository.save(alert));
    }

    public AlertResponse update(Long id, AlertRequest request) {
        Alert alert = getEntity(id);
        apply(alert, request);
        return toResponse(alertRepository.save(alert));
    }

    public AlertResponse acknowledge(Long id) {
        Alert alert = getEntity(id);
        if ("RESOLVED".equalsIgnoreCase(alert.getStatus())) {
            throw new IllegalArgumentException("A resolved alert cannot be acknowledged.");
        }
        alert.setStatus("ACKNOWLEDGED");
        alert.setAcknowledgedAt(OffsetDateTime.now());
        return toResponse(alertRepository.save(alert));
    }

    public AlertResponse resolve(Long id) {
        Alert alert = getEntity(id);
        alert.setStatus("RESOLVED");
        if (alert.getAcknowledgedAt() == null) alert.setAcknowledgedAt(OffsetDateTime.now());
        alert.setResolvedAt(OffsetDateTime.now());
        return toResponse(alertRepository.save(alert));
    }

    private Alert getEntity(Long id) {
        return alertRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Alert", id));
    }

    private void apply(Alert alert, AlertRequest request) {
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", request.getVehicleId()));
        Driver driver = request.getDriverId() == null ? null : driverRepository.findById(request.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver", request.getDriverId()));
        alert.setVehicle(vehicle);
        alert.setDriver(driver);
        alert.setAlertType(request.getAlertType());
        if (request.getSeverity() != null) alert.setSeverity(request.getSeverity());
        alert.setDescription(request.getDescription());
        alert.setLatitude(request.getLatitude());
        alert.setLongitude(request.getLongitude());
        if (request.getStatus() != null) alert.setStatus(request.getStatus());
    }

    private AlertResponse toResponse(Alert alert) {
        AlertResponse response = new AlertResponse();
        response.setId(alert.getId());
        response.setVehicleId(alert.getVehicle().getId());
        response.setDriverId(alert.getDriver() == null ? null : alert.getDriver().getId());
        response.setAlertType(alert.getAlertType());
        response.setSeverity(alert.getSeverity());
        response.setDescription(alert.getDescription());
        response.setLatitude(alert.getLatitude());
        response.setLongitude(alert.getLongitude());
        response.setStatus(alert.getStatus());
        response.setCreatedAt(alert.getCreatedAt());
        response.setAcknowledgedAt(alert.getAcknowledgedAt());
        response.setResolvedAt(alert.getResolvedAt());
        return response;
    }
}
