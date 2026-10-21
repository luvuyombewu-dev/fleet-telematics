package za.co.fleettelematics.service;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import za.co.fleettelematics.dto.request.GpsEventRequest;
import za.co.fleettelematics.dto.response.GpsEventResponse;
import za.co.fleettelematics.entity.Driver;
import za.co.fleettelematics.entity.GpsEvent;
import za.co.fleettelematics.entity.Vehicle;
import za.co.fleettelematics.exception.ResourceNotFoundException;
import za.co.fleettelematics.repository.DriverRepository;
import za.co.fleettelematics.repository.GpsEventRepository;
import za.co.fleettelematics.repository.VehicleRepository;

@Service
@Transactional
public class GpsEventService {
    private final GpsEventRepository gpsEventRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;

    public GpsEventService(GpsEventRepository gpsEventRepository, VehicleRepository vehicleRepository,
                           DriverRepository driverRepository) {
        this.gpsEventRepository = gpsEventRepository;
        this.vehicleRepository = vehicleRepository;
        this.driverRepository = driverRepository;
    }

    public GpsEventResponse create(GpsEventRequest request) {
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", request.getVehicleId()));
        Driver driver = request.getDriverId() == null ? null : driverRepository.findById(request.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver", request.getDriverId()));
        GpsEvent event = new GpsEvent();
        event.setVehicle(vehicle);
        event.setDriver(driver);
        event.setEventType(request.getEventType());
        event.setLatitude(request.getLatitude());
        event.setLongitude(request.getLongitude());
        event.setMetadata(request.getMetadata());
        event.setRecordedAt(request.getRecordedAt() == null ? OffsetDateTime.now() : request.getRecordedAt());
        return toResponse(gpsEventRepository.save(event));
    }

    public GpsEventResponse update(Long id, GpsEventRequest request) {
        GpsEvent event = gpsEventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GPS event", id));
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", request.getVehicleId()));
        Driver driver = request.getDriverId() == null ? null : driverRepository.findById(request.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver", request.getDriverId()));
        event.setVehicle(vehicle);
        event.setDriver(driver);
        event.setEventType(request.getEventType());
        event.setLatitude(request.getLatitude());
        event.setLongitude(request.getLongitude());
        event.setMetadata(request.getMetadata());
        if (request.getRecordedAt() != null) event.setRecordedAt(request.getRecordedAt());
        return toResponse(gpsEventRepository.save(event));
    }

    public void delete(Long id) {
        GpsEvent event = gpsEventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GPS event", id));
        gpsEventRepository.delete(event);
    }

    @Transactional(readOnly = true)
    public List<GpsEventResponse> findAll() {
        return gpsEventRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<GpsEventResponse> findByVehicle(Long vehicleId) {
        if (!vehicleRepository.existsById(vehicleId)) throw new ResourceNotFoundException("Vehicle", vehicleId);
        return gpsEventRepository.findByVehicleId(vehicleId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<GpsEventResponse> findByDriver(Long driverId) {
        if (!driverRepository.existsById(driverId)) throw new ResourceNotFoundException("Driver", driverId);
        return gpsEventRepository.findByDriverId(driverId).stream().map(this::toResponse).toList();
    }

    private GpsEventResponse toResponse(GpsEvent event) {
        GpsEventResponse response = new GpsEventResponse();
        response.setId(event.getId());
        response.setVehicleId(event.getVehicle().getId());
        response.setDriverId(event.getDriver() == null ? null : event.getDriver().getId());
        response.setEventType(event.getEventType());
        response.setLatitude(event.getLatitude());
        response.setLongitude(event.getLongitude());
        response.setMetadata(event.getMetadata());
        response.setRecordedAt(event.getRecordedAt());
        return response;
    }
}
