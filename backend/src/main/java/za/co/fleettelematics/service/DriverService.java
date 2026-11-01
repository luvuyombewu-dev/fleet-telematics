package za.co.fleettelematics.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import za.co.fleettelematics.dto.request.DriverRequest;
import za.co.fleettelematics.dto.response.DriverResponse;
import za.co.fleettelematics.entity.Driver;
import za.co.fleettelematics.exception.ResourceNotFoundException;
import za.co.fleettelematics.repository.DriverRepository;

@Service
@Transactional
public class DriverService {
    private final DriverRepository driverRepository;

    public DriverService(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    @Transactional(readOnly = true)
    public List<DriverResponse> findAll() {
        return driverRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public DriverResponse findById(Long id) {
        return toResponse(getEntity(id));
    }

    public DriverResponse create(DriverRequest request) {
        Driver driver = new Driver();
        apply(driver, request);
        driver.setEmployeeNumber(generateTemporaryEmployeeNumber());

        Driver saved = driverRepository.saveAndFlush(driver);
        saved.setEmployeeNumber(generateEmployeeNumber(saved.getId()));
        return toResponse(driverRepository.saveAndFlush(saved));
    }

    public DriverResponse update(Long id, DriverRequest request) {
        Driver driver = getEntity(id);
        apply(driver, request);
        // Employee numbers are immutable system identifiers and are never changed by PUT.
        return toResponse(driverRepository.save(driver));
    }

    public void delete(Long id) {
        driverRepository.delete(getEntity(id));
    }

    private String generateEmployeeNumber(Long id) {
        String base = String.format("EMP-%03d", id);
        String candidate = base;
        int suffix = 1;
        while (driverRepository.findByEmployeeNumber(candidate).isPresent()) {
            candidate = base + "-" + suffix++;
        }
        return candidate;
    }

    private String generateTemporaryEmployeeNumber() {
        String candidate;
        do {
            candidate = "TMP-EMP-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        } while (driverRepository.findByEmployeeNumber(candidate).isPresent());
        return candidate;
    }

    private Driver getEntity(Long id) {
        return driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", id));
    }

    private void apply(Driver driver, DriverRequest request) {
        driver.setFirstName(request.getFirstName());
        driver.setLastName(request.getLastName());
        driver.setEmail(request.getEmail());
        driver.setPhone(request.getPhone());
        driver.setLicenceNumber(request.getLicenceNumber());
        driver.setLicenceExpiryDate(request.getLicenceExpiryDate());
        if (request.getStatus() != null) driver.setStatus(request.getStatus());
    }

    private DriverResponse toResponse(Driver driver) {
        DriverResponse response = new DriverResponse();
        response.setId(driver.getId());
        response.setEmployeeNumber(driver.getEmployeeNumber());
        response.setFirstName(driver.getFirstName());
        response.setLastName(driver.getLastName());
        response.setEmail(driver.getEmail());
        response.setPhone(driver.getPhone());
        response.setLicenceNumber(driver.getLicenceNumber());
        response.setLicenceExpiryDate(driver.getLicenceExpiryDate());
        response.setStatus(driver.getStatus());
        response.setCreatedAt(driver.getCreatedAt());
        response.setUpdatedAt(driver.getUpdatedAt());
        return response;
    }
}
