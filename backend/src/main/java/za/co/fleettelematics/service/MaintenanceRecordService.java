package za.co.fleettelematics.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import za.co.fleettelematics.dto.request.MaintenanceRecordRequest;
import za.co.fleettelematics.dto.response.MaintenanceRecordResponse;
import za.co.fleettelematics.entity.MaintenanceRecord;
import za.co.fleettelematics.entity.Vehicle;
import za.co.fleettelematics.exception.ResourceNotFoundException;
import za.co.fleettelematics.repository.MaintenanceRecordRepository;
import za.co.fleettelematics.repository.VehicleRepository;

@Service
@Transactional
public class MaintenanceRecordService {
    private final MaintenanceRecordRepository maintenanceRepository;
    private final VehicleRepository vehicleRepository;

    public MaintenanceRecordService(MaintenanceRecordRepository maintenanceRepository,
                                    VehicleRepository vehicleRepository) {
        this.maintenanceRepository = maintenanceRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional(readOnly = true)
    public List<MaintenanceRecordResponse> findAll(Long vehicleId, String status) {
        return maintenanceRepository.findAll().stream()
                .filter(record -> vehicleId == null || record.getVehicle().getId().equals(vehicleId))
                .filter(record -> status == null || status.equalsIgnoreCase(record.getStatus()))
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public MaintenanceRecordResponse findById(Long id) { return toResponse(getEntity(id)); }

    public MaintenanceRecordResponse create(MaintenanceRecordRequest request) {
        MaintenanceRecord record = new MaintenanceRecord();
        apply(record, request);
        return toResponse(maintenanceRepository.save(record));
    }

    public MaintenanceRecordResponse update(Long id, MaintenanceRecordRequest request) {
        MaintenanceRecord record = getEntity(id);
        apply(record, request);
        return toResponse(maintenanceRepository.save(record));
    }

    public void delete(Long id) { maintenanceRepository.delete(getEntity(id)); }

    private MaintenanceRecord getEntity(Long id) {
        return maintenanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance record", id));
    }

    private void apply(MaintenanceRecord record, MaintenanceRecordRequest request) {
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", request.getVehicleId()));
        record.setVehicle(vehicle);
        record.setMaintenanceType(request.getMaintenanceType());
        record.setDescription(request.getDescription());
        record.setScheduledDate(request.getScheduledDate());
        record.setCompletedDate(request.getCompletedDate());
        record.setServiceMileage(request.getServiceMileage());
        record.setCost(request.getCost());
        if (request.getStatus() != null) record.setStatus(request.getStatus());
        record.setNotes(request.getNotes());
    }

    private MaintenanceRecordResponse toResponse(MaintenanceRecord record) {
        MaintenanceRecordResponse response = new MaintenanceRecordResponse();
        response.setId(record.getId());
        response.setVehicleId(record.getVehicle().getId());
        response.setMaintenanceType(record.getMaintenanceType());
        response.setDescription(record.getDescription());
        response.setScheduledDate(record.getScheduledDate());
        response.setCompletedDate(record.getCompletedDate());
        response.setServiceMileage(record.getServiceMileage());
        response.setCost(record.getCost());
        response.setStatus(record.getStatus());
        response.setNotes(record.getNotes());
        response.setCreatedAt(record.getCreatedAt());
        response.setUpdatedAt(record.getUpdatedAt());
        return response;
    }
}
