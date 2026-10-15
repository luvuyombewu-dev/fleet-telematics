package za.co.fleettelematics.controller;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.fleettelematics.dto.request.TripPointRequest;
import za.co.fleettelematics.dto.response.TripPointResponse;
import za.co.fleettelematics.service.TripPointService;

@RestController
@RequestMapping("/api/v1/trip-points")
public class TripPointController {
    private final TripPointService service;
    public TripPointController(TripPointService service) { this.service = service; }
    @PostMapping public ResponseEntity<TripPointResponse> create(@Valid @RequestBody TripPointRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }
    @GetMapping("/trip/{tripId}") public List<TripPointResponse> findByTrip(@PathVariable Long tripId) {
        return service.findByTrip(tripId);
    }
}
