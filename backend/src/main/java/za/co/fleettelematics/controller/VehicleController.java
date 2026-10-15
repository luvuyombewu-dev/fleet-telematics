package za.co.fleettelematics.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import za.co.fleettelematics.dto.request.VehicleRequest;
import za.co.fleettelematics.dto.response.VehicleResponse;
import za.co.fleettelematics.service.VehicleService;

@RestController
@RequestMapping("/api/v1/vehicles")
public class VehicleController {
    private final VehicleService vehicleService;
    public VehicleController(VehicleService vehicleService) { this.vehicleService = vehicleService; }

    @GetMapping public List<VehicleResponse> findAll() { return vehicleService.findAll(); }
    @GetMapping("/{id}") public VehicleResponse findById(@PathVariable Long id) { return vehicleService.findById(id); }
    @PostMapping public ResponseEntity<VehicleResponse> create(@Valid @RequestBody VehicleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleService.create(request));
    }
    @PutMapping("/{id}") public VehicleResponse update(@PathVariable Long id, @Valid @RequestBody VehicleRequest request) {
        return vehicleService.update(id, request);
    }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) {
        vehicleService.delete(id); return ResponseEntity.noContent().build();
    }
}
