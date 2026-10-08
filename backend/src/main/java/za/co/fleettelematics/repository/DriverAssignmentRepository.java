package za.co.fleettelematics.repository;

import za.co.fleettelematics.entity.DriverAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DriverAssignmentRepository extends JpaRepository<DriverAssignment, Long> {

    List<DriverAssignment> findByStatus(String status);

    List<DriverAssignment> findByDriverId(Long driverId);

    List<DriverAssignment> findByVehicleId(Long vehicleId);
}
