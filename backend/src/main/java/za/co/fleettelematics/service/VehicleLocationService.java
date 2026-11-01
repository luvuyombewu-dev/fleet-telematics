package za.co.fleettelematics.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import za.co.fleettelematics.exception.ResourceNotFoundException;
import za.co.fleettelematics.repository.TelemetryRepository;
import za.co.fleettelematics.repository.VehicleRepository;

@Service
@Transactional(readOnly = true)
public class VehicleLocationService {
    private final TelemetryRepository telemetryRepository;
    private final VehicleRepository vehicleRepository;

    public VehicleLocationService(TelemetryRepository telemetryRepository, VehicleRepository vehicleRepository) {
        this.telemetryRepository = telemetryRepository;
        this.vehicleRepository = vehicleRepository;
    }

    public Optional<Position> findLatestPosition(Long vehicleId) {
        if (!vehicleRepository.existsById(vehicleId)) {
            throw new ResourceNotFoundException("Vehicle", vehicleId);
        }
        return telemetryRepository.findTop100ByVehicleIdOrderByRecordedAtDesc(vehicleId).stream()
                .filter(sample -> sample.getLatitude() != null && sample.getLongitude() != null)
                .findFirst()
                .map(sample -> new Position(sample.getLatitude(), sample.getLongitude(), sample.getRecordedAt()));
    }

    public Position requireLatestPosition(Long vehicleId) {
        return findLatestPosition(vehicleId).orElseThrow(() ->
                new IllegalArgumentException("No saved GPS position is available for this vehicle. Receive a telemetry sample first."));
    }

    public record Position(BigDecimal latitude, BigDecimal longitude, OffsetDateTime recordedAt) {
    }
}
