package za.co.fleettelematics.repository;

import za.co.fleettelematics.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    Optional<Vehicle> findByRegistrationNumber(String registrationNumber);

    Optional<Vehicle> findByFleetNumber(String fleetNumber);

    Optional<Vehicle> findByVin(String vin);

    Optional<Vehicle> findByDeviceId(String deviceId);
}
