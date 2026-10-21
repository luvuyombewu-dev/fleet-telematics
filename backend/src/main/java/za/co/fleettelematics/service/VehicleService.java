package za.co.fleettelematics.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import za.co.fleettelematics.dto.request.VehicleRequest;
import za.co.fleettelematics.dto.response.VehicleResponse;
import za.co.fleettelematics.entity.Vehicle;
import za.co.fleettelematics.exception.ResourceNotFoundException;
import za.co.fleettelematics.repository.VehicleRepository;

@Service
@Transactional
public class VehicleService {
    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> findAll() {
        return vehicleRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public VehicleResponse findById(Long id) {
        return toResponse(getEntity(id));
    }

    public VehicleResponse create(VehicleRequest request) {
        Vehicle vehicle = new Vehicle();
        apply(vehicle, request);

        // Both temporary values stay well below the database VARCHAR(30) limit.
        vehicle.setFleetNumber(generateTemporaryFleetNumber());
        vehicle.setDeviceId(generateTemporaryDeviceId());
        Vehicle saved = vehicleRepository.saveAndFlush(vehicle);
        saved.setFleetNumber(generateFleetNumber(saved.getId()));
        saved.setDeviceId(generateDeviceId(saved.getId()));
        return toResponse(vehicleRepository.saveAndFlush(saved));
    }

    public VehicleResponse update(Long id, VehicleRequest request) {
        Vehicle vehicle = getEntity(id);
        apply(vehicle, request);
        if (vehicle.getFleetNumber() == null || vehicle.getFleetNumber().isBlank()) {
            vehicle.setFleetNumber(generateFleetNumber(vehicle.getId()));
        }
        if (vehicle.getDeviceId() == null || vehicle.getDeviceId().isBlank()) {
            vehicle.setDeviceId(generateDeviceId(vehicle.getId()));
        }
        return toResponse(vehicleRepository.saveAndFlush(vehicle));
    }

    public void delete(Long id) {
        vehicleRepository.delete(getEntity(id));
    }

    private String generateFleetNumber(Long id) {
        String base = String.format("FLT-%03d", id);
        String candidate = base;
        int suffix = 1;
        while (fleetNumberExists(candidate)) {
            candidate = base + "-" + suffix++;
        }
        if (candidate.length() > 30) {
            throw new IllegalStateException("Generated fleet number exceeds VARCHAR(30).");
        }
        return candidate;
    }

    private boolean fleetNumberExists(String candidate) {
        return vehicleRepository.findAll().stream().anyMatch(existing ->
            existing.getFleetNumber() != null && existing.getFleetNumber().equalsIgnoreCase(candidate));
    }

    private Vehicle getEntity(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", id));
    }

    private void apply(Vehicle vehicle, VehicleRequest request) {
        vehicle.setRegistrationNumber(request.getRegistrationNumber());
        vehicle.setVin(request.getVin());
        vehicle.setMake(request.getMake());
        vehicle.setModel(request.getModel());
        vehicle.setVehicleYear(request.getVehicleYear());
        vehicle.setVehicleType(request.getVehicleType());
        vehicle.setFuelType(request.getFuelType());
        if (request.getMileage() != null) vehicle.setMileage(request.getMileage());
        if (request.getStatus() != null) {
            String status = request.getStatus().trim().toUpperCase();
            if (!List.of("ACTIVE", "INACTIVE", "SUSPENDED").contains(status)) {
                throw new IllegalArgumentException("Vehicle status must be ACTIVE, INACTIVE, or SUSPENDED.");
            }
            vehicle.setStatus(status);
        }
if (request.getTrackingStatus() != null) vehicle.setTrackingStatus(request.getTrackingStatus());
    }

    private VehicleResponse toResponse(Vehicle vehicle) {
        VehicleResponse response = new VehicleResponse();
        response.setId(vehicle.getId());
        response.setRegistrationNumber(vehicle.getRegistrationNumber());
        response.setFleetNumber(vehicle.getFleetNumber());
        response.setVin(vehicle.getVin());
        response.setMake(vehicle.getMake());
        response.setModel(vehicle.getModel());
        response.setVehicleYear(vehicle.getVehicleYear());
        response.setVehicleType(vehicle.getVehicleType());
        response.setFuelType(vehicle.getFuelType());
        response.setMileage(vehicle.getMileage());
        response.setStatus(vehicle.getStatus());
        response.setDeviceId(vehicle.getDeviceId());
        response.setTrackingStatus(vehicle.getTrackingStatus());
        response.setCreatedAt(vehicle.getCreatedAt());
        response.setUpdatedAt(vehicle.getUpdatedAt());
        return response;
    }

    private String generateDeviceId(Long id) {
            String base = String.format("GPS-DEV-%03d", id);
            String candidate = base;
            int suffix = 1;
            while (deviceIdExists(candidate)) {
                candidate = base + "-" + suffix++;
            }
            if (candidate.length() > 30) {
                throw new IllegalStateException("Generated tracking device ID exceeds VARCHAR(30).");
            }
            return candidate;
        }

    private boolean deviceIdExists(String candidate) {
            return vehicleRepository.findAll().stream().anyMatch(existing ->
                existing.getDeviceId() != null && existing.getDeviceId().equalsIgnoreCase(candidate));
        }

    private String generateTemporaryFleetNumber() {
            String candidate;
            do {
                candidate = "TMP-FLT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            } while (fleetNumberExists(candidate));
            return candidate;
        }

    private String generateTemporaryDeviceId() {
            String candidate;
            do {
                candidate = "TMP-DEV-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            } while (deviceIdExists(candidate));
            return candidate;
        }
}
