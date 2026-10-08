package za.co.fleettelematics.repository;

import za.co.fleettelematics.entity.GpsEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GpsEventRepository extends JpaRepository<GpsEvent, Long> {

    List<GpsEvent> findByVehicleId(Long vehicleId);

    List<GpsEvent> findByDriverId(Long driverId);
}
