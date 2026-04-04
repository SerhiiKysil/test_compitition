package org.example.backend.service;

import org.example.backend.dto.LocationRequest;
import org.example.backend.entity.Location;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.repository.LocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class LocationService {

    private final LocationRepository repository;

    public LocationService(LocationRepository repository) {
        this.repository = repository;
    }

    public List<Location> getAll() {
        return repository.findAll();
    }

    public Location getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location", id));
    }

    @Transactional
    public Location create(LocationRequest req) {
        return repository.save(Location.builder()
                .name(req.name()).address(req.address()).city(req.city()).country(req.country())
                .latitude(req.latitude()).longitude(req.longitude())
                .hasPort(req.hasPort()).hasAirport(req.hasAirport())
                .hasRailTerminal(req.hasRailTerminal()).hasRoadAccess(req.hasRoadAccess())
                .build());
    }

    @Transactional
    public Location update(Long id, LocationRequest req) {
        Location l = getById(id);
        l.setName(req.name()); l.setAddress(req.address());
        l.setCity(req.city()); l.setCountry(req.country());
        l.setLatitude(req.latitude()); l.setLongitude(req.longitude());
        l.setHasPort(req.hasPort()); l.setHasAirport(req.hasAirport());
        l.setHasRailTerminal(req.hasRailTerminal()); l.setHasRoadAccess(req.hasRoadAccess());
        return repository.save(l);
    }

    @Transactional
    public void delete(Long id) {
        getById(id);
        repository.deleteById(id);
    }
}
