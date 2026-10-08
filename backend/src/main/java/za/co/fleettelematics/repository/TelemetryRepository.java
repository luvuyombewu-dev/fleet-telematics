package za.co.fleettelematics.repository;

import za.co.fleettelematics.entity.Telemetry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TelemetryRepository extends JpaRepository<Telemetry, Long> {

    List<Telemetry> findByVehicleId(Long vehicleId);

    List<Telemetry> findTop100ByVehicleIdOrderByRecordedAtDesc(Long vehicleId);
}
