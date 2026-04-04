package org.example.backend.service;

import org.example.backend.dto.TransportRequest;
import org.example.backend.entity.Location;
import org.example.backend.entity.Transport;
import org.example.backend.enums.TransportStatus;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.repository.LocationRepository;
import org.example.backend.repository.TransportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class TransportService {

    private final TransportRepository repository;
    private final LocationRepository locationRepository;

    public TransportService(TransportRepository repository, LocationRepository locationRepository) {
        this.repository = repository;
        this.locationRepository = locationRepository;
    }

    public List<Transport> getAll() { return repository.findAll(); }

    public Transport getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transport", id));
    }

    public List<Transport> getAvailable() {
        return repository.findByStatus(TransportStatus.AVAILABLE);
    }

    @Transactional
    public Transport create(TransportRequest req) {
        Location loc = resolveLocation(req.currentLocationId());
        return repository.save(Transport.builder()
                .name(req.name()).type(req.type())
                .maxCargoWeightTons(req.maxCargoWeightTons()).maxCargoVolumeM3(req.maxCargoVolumeM3())
                .fuelType(req.fuelType()).fuelCapacityLiters(req.fuelCapacityLiters())
                .rangeKm(req.rangeKm())
                .status(req.status() != null ? req.status() : TransportStatus.AVAILABLE)
                .currentLocation(loc).description(req.description())
                .build());
    }

    @Transactional
    public Transport update(Long id, TransportRequest req) {
        Transport t = getById(id);
        t.setName(req.name()); t.setType(req.type());
        t.setMaxCargoWeightTons(req.maxCargoWeightTons()); t.setMaxCargoVolumeM3(req.maxCargoVolumeM3());
        t.setFuelType(req.fuelType()); t.setFuelCapacityLiters(req.fuelCapacityLiters());
        t.setRangeKm(req.rangeKm());
        if (req.status() != null) t.setStatus(req.status());
        t.setCurrentLocation(resolveLocation(req.currentLocationId()));
        t.setDescription(req.description());
        return repository.save(t);
    }

    @Transactional
    public Transport updateStatus(Long id, TransportStatus status) {
        Transport t = getById(id);
        t.setStatus(status);
        return repository.save(t);
    }

    @Transactional
    public Transport updateLocation(Long id, Long locationId) {
        Transport t = getById(id);
        t.setCurrentLocation(locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location", locationId)));
        return repository.save(t);
    }

    @Transactional
    public void delete(Long id) {
        getById(id);
        repository.deleteById(id);
    }

    private Location resolveLocation(Long locationId) {
        if (locationId == null) return null;
        return locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location", locationId));
    }
}
