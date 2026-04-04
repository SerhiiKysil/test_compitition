package org.example.backend.controller;

import jakarta.validation.Valid;
import org.example.backend.dto.WarehouseProductRequest;
import org.example.backend.dto.WarehouseRequest;
import org.example.backend.entity.Warehouse;
import org.example.backend.entity.WarehouseProduct;
import org.example.backend.service.WarehouseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warehouses")
public class WarehouseController {

    private final WarehouseService service;
    public WarehouseController(WarehouseService service) { this.service = service; }

    @GetMapping
    public List<Warehouse> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public Warehouse getById(@PathVariable Long id) { return service.getById(id); }

    @GetMapping("/{id}/products")
    public List<WarehouseProduct> getProducts(@PathVariable Long id) { return service.getProducts(id); }

    @PostMapping
    public ResponseEntity<Warehouse> create(@Valid @RequestBody WarehouseRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @PutMapping("/{id}")
    public Warehouse update(@PathVariable Long id, @Valid @RequestBody WarehouseRequest req) {
        return service.update(id, req);
    }

    @PostMapping("/{id}/products")
    public ResponseEntity<WarehouseProduct> addProduct(@PathVariable Long id,
                                                        @Valid @RequestBody WarehouseProductRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addOrUpdateProduct(id, req));
    }

    @PutMapping("/{id}/products/{productId}")
    public WarehouseProduct updateProduct(@PathVariable Long id, @PathVariable Long productId,
                                           @Valid @RequestBody WarehouseProductRequest req) {
        return service.addOrUpdateProduct(id, new WarehouseProductRequest(productId, req.quantity()));
    }

    @DeleteMapping("/{id}/products/{productId}")
    public ResponseEntity<Void> removeProduct(@PathVariable Long id, @PathVariable Long productId) {
        service.removeProduct(id, productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
