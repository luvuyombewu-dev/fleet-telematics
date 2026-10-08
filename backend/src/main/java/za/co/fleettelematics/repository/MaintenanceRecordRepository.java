package za.co.fleettelematics.repository;

import za.co.fleettelematics.entity.MaintenanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, Long> {

    List<MaintenanceRecord> findByVehicleId(Long vehicleId);

    List<MaintenanceRecord> findByStatus(String status);
}
