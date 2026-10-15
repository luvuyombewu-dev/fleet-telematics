package za.co.fleettelematics.controller;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.fleettelematics.dto.request.TelemetryRequest;
import za.co.fleettelematics.dto.response.TelemetryResponse;
import za.co.fleettelematics.service.TelemetryService;

@RestController
@RequestMapping("/api/v1/telemetry")
public class TelemetryController {
    private final TelemetryService service;
    public TelemetryController(TelemetryService service) { this.service = service; }
    @PostMapping public ResponseEntity<TelemetryResponse> record(@Valid @RequestBody TelemetryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.record(request));
    }
    @GetMapping("/vehicle/{vehicleId}") public List<TelemetryResponse> findRecent(@PathVariable Long vehicleId) {
        return service.findRecentByVehicle(vehicleId);
    }
    @GetMapping("/vehicle/{vehicleId}/latest") public TelemetryResponse findLatest(@PathVariable Long vehicleId) {
        return service.findLatestByVehicle(vehicleId);
    }
}
