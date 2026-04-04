package org.example.backend.controller;

import jakarta.validation.Valid;
import org.example.backend.dto.ConsumerRequest;
import org.example.backend.entity.Consumer;
import org.example.backend.service.ConsumerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consumers")
public class ConsumerController {

    private final ConsumerService service;
    public ConsumerController(ConsumerService service) { this.service = service; }

    @GetMapping
    public List<Consumer> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public Consumer getById(@PathVariable Long id) { return service.getById(id); }

    @PostMapping
    public ResponseEntity<Consumer> create(@Valid @RequestBody ConsumerRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @PutMapping("/{id}")
    public Consumer update(@PathVariable Long id, @Valid @RequestBody ConsumerRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
