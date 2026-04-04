package org.example.backend.controller;

import jakarta.validation.Valid;
import org.example.backend.dto.LocationRequest;
import org.example.backend.entity.Location;
import org.example.backend.service.LocationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationService service;
    public LocationController(LocationService service) { this.service = service; }

    @GetMapping
    public List<Location> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public Location getById(@PathVariable Long id) { return service.getById(id); }

    @PostMapping
    public ResponseEntity<Location> create(@Valid @RequestBody LocationRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @PutMapping("/{id}")
    public Location update(@PathVariable Long id, @Valid @RequestBody LocationRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
