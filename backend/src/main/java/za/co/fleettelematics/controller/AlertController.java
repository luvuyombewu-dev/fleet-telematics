package za.co.fleettelematics.controller;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.fleettelematics.dto.request.AlertRequest;
import za.co.fleettelematics.dto.response.AlertResponse;
import za.co.fleettelematics.service.AlertService;

@RestController
@RequestMapping("/api/v1/alerts")
public class AlertController {
    private final AlertService service;
    public AlertController(AlertService service) { this.service = service; }
    @GetMapping public List<AlertResponse> findAll(
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(required = false) Long driverId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String severity) {
        return service.findAll(vehicleId, driverId, status, severity);
    }
    @GetMapping("/{id}") public AlertResponse findById(@PathVariable Long id) { return service.findById(id); }
    @PostMapping public ResponseEntity<AlertResponse> create(@Valid @RequestBody AlertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }
    @PutMapping("/{id}") public AlertResponse update(@PathVariable Long id, @Valid @RequestBody AlertRequest request) {
        return service.update(id, request);
    }
    @PostMapping("/{id}/acknowledge") public AlertResponse acknowledge(@PathVariable Long id) { return service.acknowledge(id); }
    @PostMapping("/{id}/resolve") public AlertResponse resolve(@PathVariable Long id) { return service.resolve(id); }
}
