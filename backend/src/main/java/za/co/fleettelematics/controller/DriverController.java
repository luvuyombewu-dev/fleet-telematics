package za.co.fleettelematics.controller;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.fleettelematics.dto.request.DriverRequest;
import za.co.fleettelematics.dto.response.DriverResponse;
import za.co.fleettelematics.service.DriverService;

@RestController
@RequestMapping("/api/v1/drivers")
public class DriverController {
    private final DriverService driverService;
    public DriverController(DriverService driverService) { this.driverService = driverService; }
    @GetMapping public List<DriverResponse> findAll() { return driverService.findAll(); }
    @GetMapping("/{id}") public DriverResponse findById(@PathVariable Long id) { return driverService.findById(id); }
    @PostMapping public ResponseEntity<DriverResponse> create(@Valid @RequestBody DriverRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(driverService.create(request));
    }
    @PutMapping("/{id}") public DriverResponse update(@PathVariable Long id, @Valid @RequestBody DriverRequest request) {
        return driverService.update(id, request);
    }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) {
        driverService.delete(id); return ResponseEntity.noContent().build();
    }
}
