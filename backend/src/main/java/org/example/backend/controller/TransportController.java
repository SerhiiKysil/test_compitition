package org.example.backend.controller;

import jakarta.validation.Valid;
import org.example.backend.dto.StatusUpdateRequest;
import org.example.backend.dto.TransportRequest;
import org.example.backend.entity.Transport;
import org.example.backend.enums.TransportStatus;
import org.example.backend.service.TransportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transports")
public class TransportController {

    private final TransportService service;
    public TransportController(TransportService service) { this.service = service; }

    @GetMapping
    public List<Transport> getAll() { return service.getAll(); }

    @GetMapping("/available")
    public List<Transport> getAvailable() { return service.getAvailable(); }

    @GetMapping("/{id}")
    public Transport getById(@PathVariable Long id) { return service.getById(id); }

    @PostMapping
    public ResponseEntity<Transport> create(@Valid @RequestBody TransportRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @PutMapping("/{id}")
    public Transport update(@PathVariable Long id, @Valid @RequestBody TransportRequest req) {
        return service.update(id, req);
    }

    @PatchMapping("/{id}/status")
    public Transport updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest req) {
        return service.updateStatus(id, TransportStatus.valueOf(req.status().toUpperCase()));
    }

    @PatchMapping("/{id}/location")
    public Transport updateLocation(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        return service.updateLocation(id, body.get("locationId"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
