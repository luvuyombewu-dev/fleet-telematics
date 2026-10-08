package za.co.fleettelematics.repository;

import za.co.fleettelematics.entity.Geofence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GeofenceRepository extends JpaRepository<Geofence, Long> {

    Optional<Geofence> findByName(String name);

    java.util.List<Geofence> findByStatus(String status);
}
