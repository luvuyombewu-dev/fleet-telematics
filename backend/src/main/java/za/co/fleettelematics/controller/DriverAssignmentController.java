package za.co.fleettelematics.controller;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.fleettelematics.dto.request.DriverAssignmentRequest;
import za.co.fleettelematics.dto.response.DriverAssignmentResponse;
import za.co.fleettelematics.service.DriverAssignmentService;

@RestController
@RequestMapping("/api/v1/driver-assignments")
public class DriverAssignmentController {
    private final DriverAssignmentService service;
    public DriverAssignmentController(DriverAssignmentService service) { this.service = service; }
    @GetMapping public List<DriverAssignmentResponse> findAll(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long driverId,
            @RequestParam(required = false) Long vehicleId) {
        return service.findAll(status, driverId, vehicleId);
    }
    @GetMapping("/{id}") public DriverAssignmentResponse findById(@PathVariable Long id) { return service.findById(id); }
    @PostMapping public ResponseEntity<DriverAssignmentResponse> assign(@Valid @RequestBody DriverAssignmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.assign(request));
    }
    @PostMapping("/{id}/unassign") public DriverAssignmentResponse unassign(@PathVariable Long id) { return service.unassign(id); }
}
