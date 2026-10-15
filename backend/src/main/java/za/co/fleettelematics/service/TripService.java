package za.co.fleettelematics.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import za.co.fleettelematics.dto.request.TripRequest;
import za.co.fleettelematics.dto.response.TripResponse;
import za.co.fleettelematics.entity.Driver;
import za.co.fleettelematics.entity.Trip;
import za.co.fleettelematics.entity.Vehicle;
import za.co.fleettelematics.exception.ResourceNotFoundException;
import za.co.fleettelematics.repository.DriverRepository;
import za.co.fleettelematics.repository.TripRepository;
import za.co.fleettelematics.repository.VehicleRepository;

@Service
@Transactional
public class TripService {
    private final TripRepository tripRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;

    public TripService(TripRepository tripRepository, VehicleRepository vehicleRepository,
                       DriverRepository driverRepository) {
        this.tripRepository = tripRepository;
        this.vehicleRepository = vehicleRepository;
        this.driverRepository = driverRepository;
    }

    @Transactional(readOnly = true)
    public List<TripResponse> findAll(Long vehicleId, Long driverId, String status) {
        return tripRepository.findAll().stream()
                .filter(trip -> vehicleId == null || trip.getVehicle().getId().equals(vehicleId))
                .filter(trip -> driverId == null || (trip.getDriver() != null && trip.getDriver().getId().equals(driverId)))
                .filter(trip -> status == null || status.equalsIgnoreCase(trip.getStatus()))
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public TripResponse findById(Long id) { return toResponse(getEntity(id)); }

    public TripResponse create(TripRequest request) {
        Trip trip = new Trip();
        apply(trip, request);
        return toResponse(tripRepository.save(trip));
    }

    public TripResponse update(Long id, TripRequest request) {
        Trip trip = getEntity(id);
        apply(trip, request);
        return toResponse(tripRepository.save(trip));
    }

    public void delete(Long id) { tripRepository.delete(getEntity(id)); }

    private Trip getEntity(Long id) {
        return tripRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Trip", id));
    }

    private void apply(Trip trip, TripRequest request) {
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", request.getVehicleId()));
        Driver driver = request.getDriverId() == null ? null : driverRepository.findById(request.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver", request.getDriverId()));
        trip.setVehicle(vehicle);
        trip.setDriver(driver);
        trip.setStartTime(request.getStartTime());
        trip.setEndTime(request.getEndTime());
        trip.setStartLocation(request.getStartLocation());
        trip.setDestination(request.getDestination());
        trip.setDistanceKm(request.getDistanceKm());
        trip.setAverageSpeedKmh(request.getAverageSpeedKmh());
        trip.setMaximumSpeedKmh(request.getMaximumSpeedKmh());
        trip.setDurationSeconds(request.getDurationSeconds());
        if (request.getStatus() != null) trip.setStatus(request.getStatus());
    }

    private TripResponse toResponse(Trip trip) {
        TripResponse response = new TripResponse();
        response.setId(trip.getId());
        response.setVehicleId(trip.getVehicle().getId());
        response.setDriverId(trip.getDriver() == null ? null : trip.getDriver().getId());
        response.setStartTime(trip.getStartTime());
        response.setEndTime(trip.getEndTime());
        response.setStartLocation(trip.getStartLocation());
        response.setDestination(trip.getDestination());
        response.setDistanceKm(trip.getDistanceKm());
        response.setAverageSpeedKmh(trip.getAverageSpeedKmh());
        response.setMaximumSpeedKmh(trip.getMaximumSpeedKmh());
        response.setDurationSeconds(trip.getDurationSeconds());
        response.setStatus(trip.getStatus());
        response.setCreatedAt(trip.getCreatedAt());
        response.setUpdatedAt(trip.getUpdatedAt());
        return response;
    }
}
