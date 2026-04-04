package org.example.backend.controller;

import jakarta.validation.Valid;
import org.example.backend.dto.DeliveryOrderRequest;
import org.example.backend.dto.RouteStopRequest;
import org.example.backend.dto.StatusUpdateRequest;
import org.example.backend.entity.DeliveryOrder;
import org.example.backend.entity.RouteStop;
import org.example.backend.enums.DeliveryStatus;
import org.example.backend.service.DeliveryOrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delivery-orders")
public class DeliveryOrderController {

    private final DeliveryOrderService service;
    public DeliveryOrderController(DeliveryOrderService service) { this.service = service; }

    @GetMapping
    public List<DeliveryOrder> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public DeliveryOrder getById(@PathVariable Long id) { return service.getById(id); }

    @GetMapping("/{id}/route")
    public List<RouteStop> getRoute(@PathVariable Long id) { return service.getRoute(id); }

    @PostMapping
    public ResponseEntity<DeliveryOrder> create(@Valid @RequestBody DeliveryOrderRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @PutMapping("/{id}")
    public DeliveryOrder update(@PathVariable Long id, @Valid @RequestBody DeliveryOrderRequest req) {
        return service.update(id, req);
    }

    @PatchMapping("/{id}/status")
    public DeliveryOrder updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest req) {
        return service.updateStatus(id, DeliveryStatus.valueOf(req.status().toUpperCase()));
    }

    @PostMapping("/{id}/route")
    public ResponseEntity<RouteStop> addRouteStop(@PathVariable Long id,
                                                   @Valid @RequestBody RouteStopRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addRouteStop(id, req));
    }

    @PatchMapping("/{orderId}/route/{stopId}/arrive")
    public RouteStop markArrived(@PathVariable Long orderId, @PathVariable Long stopId) {
        return service.markStopArrived(orderId, stopId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
