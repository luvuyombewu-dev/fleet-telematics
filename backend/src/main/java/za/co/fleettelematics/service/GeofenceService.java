package za.co.fleettelematics.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import za.co.fleettelematics.dto.request.GeofenceRequest;
import za.co.fleettelematics.dto.response.GeofenceResponse;
import za.co.fleettelematics.entity.Geofence;
import za.co.fleettelematics.exception.ResourceNotFoundException;
import za.co.fleettelematics.repository.GeofenceRepository;

@Service
@Transactional
public class GeofenceService {
    private final GeofenceRepository geofenceRepository;

    public GeofenceService(GeofenceRepository geofenceRepository) {
        this.geofenceRepository = geofenceRepository;
    }

    @Transactional(readOnly = true)
    public List<GeofenceResponse> findAll(String status) {
        List<Geofence> geofences = status == null ? geofenceRepository.findAll() : geofenceRepository.findByStatus(status);
        return geofences.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public GeofenceResponse findById(Long id) { return toResponse(getEntity(id)); }

    public GeofenceResponse create(GeofenceRequest request) {
        Geofence geofence = new Geofence();
        apply(geofence, request);
        return toResponse(geofenceRepository.save(geofence));
    }

    public GeofenceResponse update(Long id, GeofenceRequest request) {
        Geofence geofence = getEntity(id);
        apply(geofence, request);
        return toResponse(geofenceRepository.save(geofence));
    }

    public void delete(Long id) { geofenceRepository.delete(getEntity(id)); }

    private Geofence getEntity(Long id) {
        return geofenceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Geofence", id));
    }

    private void apply(Geofence geofence, GeofenceRequest request) {
        geofence.setName(request.getName());
        geofence.setDescription(request.getDescription());
        geofence.setLatitude(request.getLatitude());
        geofence.setLongitude(request.getLongitude());
        geofence.setRadiusMeters(request.getRadiusMeters());
        if (request.getStatus() != null) geofence.setStatus(request.getStatus());
    }

    private GeofenceResponse toResponse(Geofence geofence) {
        GeofenceResponse response = new GeofenceResponse();
        response.setId(geofence.getId());
        response.setName(geofence.getName());
        response.setDescription(geofence.getDescription());
        response.setLatitude(geofence.getLatitude());
        response.setLongitude(geofence.getLongitude());
        response.setRadiusMeters(geofence.getRadiusMeters());
        response.setStatus(geofence.getStatus());
        response.setCreatedAt(geofence.getCreatedAt());
        response.setUpdatedAt(geofence.getUpdatedAt());
        return response;
    }
}
