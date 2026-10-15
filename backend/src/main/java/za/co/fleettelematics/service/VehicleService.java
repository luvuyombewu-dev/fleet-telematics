package za.co.fleettelematics.service;

import java.util.List;

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
        return toResponse(vehicleRepository.save(vehicle));
    }

    public VehicleResponse update(Long id, VehicleRequest request) {
        Vehicle vehicle = getEntity(id);
        apply(vehicle, request);
        return toResponse(vehicleRepository.save(vehicle));
    }

    public void delete(Long id) {
        vehicleRepository.delete(getEntity(id));
    }

    private Vehicle getEntity(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", id));
    }

    private void apply(Vehicle vehicle, VehicleRequest request) {
        vehicle.setRegistrationNumber(request.getRegistrationNumber());
        vehicle.setFleetNumber(request.getFleetNumber());
        vehicle.setVin(request.getVin());
        vehicle.setMake(request.getMake());
        vehicle.setModel(request.getModel());
        vehicle.setVehicleYear(request.getVehicleYear());
        vehicle.setVehicleType(request.getVehicleType());
        vehicle.setFuelType(request.getFuelType());
        if (request.getMileage() != null) vehicle.setMileage(request.getMileage());
        if (request.getStatus() != null) vehicle.setStatus(request.getStatus());
        if (request.getDeviceId() != null) vehicle.setDeviceId(request.getDeviceId());
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
}
