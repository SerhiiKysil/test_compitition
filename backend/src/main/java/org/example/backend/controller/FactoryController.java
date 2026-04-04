package org.example.backend.controller;

import jakarta.validation.Valid;
import org.example.backend.dto.FactoryRequest;
import org.example.backend.entity.Factory;
import org.example.backend.service.FactoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/factories")
public class FactoryController {

    private final FactoryService service;
    public FactoryController(FactoryService service) { this.service = service; }

    @GetMapping
    public List<Factory> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public Factory getById(@PathVariable Long id) { return service.getById(id); }

    @PostMapping
    public ResponseEntity<Factory> create(@Valid @RequestBody FactoryRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @PutMapping("/{id}")
    public Factory update(@PathVariable Long id, @Valid @RequestBody FactoryRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
