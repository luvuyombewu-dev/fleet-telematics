package za.co.fleettelematics.repository;

import za.co.fleettelematics.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DriverRepository extends JpaRepository<Driver, Long> {

    Optional<Driver> findByEmployeeNumber(String employeeNumber);

    Optional<Driver> findByLicenceNumber(String licenceNumber);

    Optional<Driver> findByEmail(String email);
}
