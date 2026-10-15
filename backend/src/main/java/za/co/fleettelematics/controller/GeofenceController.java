package za.co.fleettelematics.controller;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.fleettelematics.dto.request.GeofenceRequest;
import za.co.fleettelematics.dto.response.GeofenceResponse;
import za.co.fleettelematics.service.GeofenceService;

@RestController
@RequestMapping("/api/v1/geofences")
public class GeofenceController {
    private final GeofenceService service;
    public GeofenceController(GeofenceService service) { this.service = service; }
    @GetMapping public List<GeofenceResponse> findAll(@RequestParam(required = false) String status) {
        return service.findAll(status);
    }
    @GetMapping("/{id}") public GeofenceResponse findById(@PathVariable Long id) { return service.findById(id); }
    @PostMapping public ResponseEntity<GeofenceResponse> create(@Valid @RequestBody GeofenceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }
    @PutMapping("/{id}") public GeofenceResponse update(@PathVariable Long id, @Valid @RequestBody GeofenceRequest request) {
        return service.update(id, request);
    }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id); return ResponseEntity.noContent().build();
    }
}
