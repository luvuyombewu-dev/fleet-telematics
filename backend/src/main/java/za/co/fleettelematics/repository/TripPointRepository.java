package za.co.fleettelematics.repository;

import za.co.fleettelematics.entity.TripPoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripPointRepository extends JpaRepository<TripPoint, Long> {

    List<TripPoint> findByTripIdOrderByRecordedAtAsc(Long tripId);
}
