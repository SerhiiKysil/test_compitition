package org.example.backend.controller;

import jakarta.validation.Valid;
import org.example.backend.dto.ServiceStationRequest;
import org.example.backend.entity.ServiceStation;
import org.example.backend.service.ServiceStationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service-stations")
public class ServiceStationController {

    private final ServiceStationService service;
    public ServiceStationController(ServiceStationService service) { this.service = service; }

    @GetMapping
    public List<ServiceStation> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public ServiceStation getById(@PathVariable Long id) { return service.getById(id); }

    @PostMapping
    public ResponseEntity<ServiceStation> create(@Valid @RequestBody ServiceStationRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @PutMapping("/{id}")
    public ServiceStation update(@PathVariable Long id, @Valid @RequestBody ServiceStationRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
