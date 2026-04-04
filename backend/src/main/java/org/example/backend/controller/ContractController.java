package org.example.backend.controller;

import jakarta.validation.Valid;
import org.example.backend.dto.ContractRequest;
import org.example.backend.dto.StatusUpdateRequest;
import org.example.backend.entity.Contract;
import org.example.backend.enums.ContractStatus;
import org.example.backend.service.ContractService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contracts")
public class ContractController {

    private final ContractService service;
    public ContractController(ContractService service) { this.service = service; }

    @GetMapping
    public List<Contract> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public Contract getById(@PathVariable Long id) { return service.getById(id); }

    @PostMapping
    public ResponseEntity<Contract> create(@Valid @RequestBody ContractRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @PutMapping("/{id}")
    public Contract update(@PathVariable Long id, @Valid @RequestBody ContractRequest req) {
        return service.update(id, req);
    }

    @PatchMapping("/{id}/status")
    public Contract updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest req) {
        return service.updateStatus(id, ContractStatus.valueOf(req.status().toUpperCase()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
