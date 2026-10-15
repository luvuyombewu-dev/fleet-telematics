package za.co.fleettelematics.controller;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.fleettelematics.dto.request.GpsEventRequest;
import za.co.fleettelematics.dto.response.GpsEventResponse;
import za.co.fleettelematics.service.GpsEventService;

@RestController
@RequestMapping("/api/v1/gps-events")
public class GpsEventController {
    private final GpsEventService service;
    public GpsEventController(GpsEventService service) { this.service = service; }
    @GetMapping public List<GpsEventResponse> findAll() { return service.findAll(); }
    @GetMapping("/vehicle/{vehicleId}") public List<GpsEventResponse> findByVehicle(@PathVariable Long vehicleId) {
        return service.findByVehicle(vehicleId);
    }
    @GetMapping("/driver/{driverId}") public List<GpsEventResponse> findByDriver(@PathVariable Long driverId) {
        return service.findByDriver(driverId);
    }
    @PostMapping public ResponseEntity<GpsEventResponse> create(@Valid @RequestBody GpsEventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }
}
