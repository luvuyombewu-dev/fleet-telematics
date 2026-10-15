package za.co.fleettelematics.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import za.co.fleettelematics.dto.request.TripPointRequest;
import za.co.fleettelematics.dto.response.TripPointResponse;
import za.co.fleettelematics.entity.Trip;
import za.co.fleettelematics.entity.TripPoint;
import za.co.fleettelematics.exception.ResourceNotFoundException;
import za.co.fleettelematics.repository.TripPointRepository;
import za.co.fleettelematics.repository.TripRepository;

@Service
@Transactional
public class TripPointService {
    private final TripPointRepository tripPointRepository;
    private final TripRepository tripRepository;

    public TripPointService(TripPointRepository tripPointRepository, TripRepository tripRepository) {
        this.tripPointRepository = tripPointRepository;
        this.tripRepository = tripRepository;
    }

    public TripPointResponse create(TripPointRequest request) {
        Trip trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException("Trip", request.getTripId()));
        TripPoint point = new TripPoint();
        point.setTrip(trip);
        point.setLatitude(request.getLatitude());
        point.setLongitude(request.getLongitude());
        point.setSpeedKmh(request.getSpeedKmh() == null ? BigDecimal.ZERO : request.getSpeedKmh());
        point.setRecordedAt(request.getRecordedAt());
        return toResponse(tripPointRepository.save(point));
    }

    @Transactional(readOnly = true)
    public List<TripPointResponse> findByTrip(Long tripId) {
        if (!tripRepository.existsById(tripId)) throw new ResourceNotFoundException("Trip", tripId);
        return tripPointRepository.findByTripIdOrderByRecordedAtAsc(tripId).stream()
                .map(this::toResponse).toList();
    }

    private TripPointResponse toResponse(TripPoint point) {
        TripPointResponse response = new TripPointResponse();
        response.setId(point.getId());
        response.setTripId(point.getTrip().getId());
        response.setLatitude(point.getLatitude());
        response.setLongitude(point.getLongitude());
        response.setSpeedKmh(point.getSpeedKmh());
        response.setRecordedAt(point.getRecordedAt());
        return response;
    }
}
