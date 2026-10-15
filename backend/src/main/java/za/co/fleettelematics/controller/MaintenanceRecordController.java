package za.co.fleettelematics.controller;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.fleettelematics.dto.request.MaintenanceRecordRequest;
import za.co.fleettelematics.dto.response.MaintenanceRecordResponse;
import za.co.fleettelematics.service.MaintenanceRecordService;

@RestController
@RequestMapping("/api/v1/maintenance-records")
public class MaintenanceRecordController {
    private final MaintenanceRecordService service;
    public MaintenanceRecordController(MaintenanceRecordService service) { this.service = service; }
    @GetMapping public List<MaintenanceRecordResponse> findAll(
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(required = false) String status) { return service.findAll(vehicleId, status); }
    @GetMapping("/{id}") public MaintenanceRecordResponse findById(@PathVariable Long id) { return service.findById(id); }
    @PostMapping public ResponseEntity<MaintenanceRecordResponse> create(@Valid @RequestBody MaintenanceRecordRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }
    @PutMapping("/{id}") public MaintenanceRecordResponse update(@PathVariable Long id,
            @Valid @RequestBody MaintenanceRecordRequest request) { return service.update(id, request); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id); return ResponseEntity.noContent().build();
    }
}
