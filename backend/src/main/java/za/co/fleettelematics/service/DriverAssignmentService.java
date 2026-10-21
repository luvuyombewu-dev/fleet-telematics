package za.co.fleettelematics.service;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import za.co.fleettelematics.dto.request.DriverAssignmentRequest;
import za.co.fleettelematics.dto.response.DriverAssignmentResponse;
import za.co.fleettelematics.entity.Driver;
import za.co.fleettelematics.entity.DriverAssignment;
import za.co.fleettelematics.entity.Vehicle;
import za.co.fleettelematics.exception.ResourceNotFoundException;
import za.co.fleettelematics.repository.DriverAssignmentRepository;
import za.co.fleettelematics.repository.DriverRepository;
import za.co.fleettelematics.repository.VehicleRepository;

@Service
@Transactional
public class DriverAssignmentService {
    private final DriverAssignmentRepository assignmentRepository;
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;

    public DriverAssignmentService(DriverAssignmentRepository assignmentRepository,
                                   DriverRepository driverRepository,
                                   VehicleRepository vehicleRepository) {
        this.assignmentRepository = assignmentRepository;
        this.driverRepository = driverRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional(readOnly = true)
    public List<DriverAssignmentResponse> findAll(String status, Long driverId, Long vehicleId) {
        return assignmentRepository.findAll().stream()
                .filter(item -> status == null || status.equalsIgnoreCase(item.getStatus()))
                .filter(item -> driverId == null || item.getDriver().getId().equals(driverId))
                .filter(item -> vehicleId == null || item.getVehicle().getId().equals(vehicleId))
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public DriverAssignmentResponse findById(Long id) {
        return toResponse(getEntity(id));
    }

    public DriverAssignmentResponse assign(DriverAssignmentRequest request) {
        Driver driver = driverRepository.findById(request.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver", request.getDriverId()));
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", request.getVehicleId()));
        DriverAssignment assignment = new DriverAssignment();
        assignment.setDriver(driver);
        assignment.setVehicle(vehicle);
        assignment.setStatus("ACTIVE");
        assignment.setAssignedAt(OffsetDateTime.now());
        return toResponse(assignmentRepository.save(assignment));
    }

    public DriverAssignmentResponse update(Long id, DriverAssignmentRequest request) {
        DriverAssignment assignment = getEntity(id);
        if (!"ACTIVE".equalsIgnoreCase(assignment.getStatus())) {
            throw new IllegalArgumentException("Only an active assignment can be edited.");
        }
        Driver driver = driverRepository.findById(request.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver", request.getDriverId()));
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", request.getVehicleId()));
        assignment.setDriver(driver);
        assignment.setVehicle(vehicle);
        return toResponse(assignmentRepository.save(assignment));
    }

    public DriverAssignmentResponse unassign(Long id) {
        DriverAssignment assignment = getEntity(id);
        if (!"ACTIVE".equalsIgnoreCase(assignment.getStatus())) {
            throw new IllegalArgumentException("Only an active assignment can be unassigned.");
        }
        assignment.setStatus("INACTIVE");
        assignment.setUnassignedAt(OffsetDateTime.now());
        return toResponse(assignmentRepository.save(assignment));
    }

    private DriverAssignment getEntity(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver assignment", id));
    }

    private DriverAssignmentResponse toResponse(DriverAssignment assignment) {
        DriverAssignmentResponse response = new DriverAssignmentResponse();
        response.setId(assignment.getId());
        response.setDriverId(assignment.getDriver().getId());
        response.setVehicleId(assignment.getVehicle().getId());
        response.setAssignedAt(assignment.getAssignedAt());
        response.setUnassignedAt(assignment.getUnassignedAt());
        response.setStatus(assignment.getStatus());
        return response;
    }
}
