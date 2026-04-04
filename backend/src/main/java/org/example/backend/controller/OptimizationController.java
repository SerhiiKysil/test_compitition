package org.example.backend.controller;

import org.example.backend.entity.Transport;
import org.example.backend.service.OptimizationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/optimization")
public class OptimizationController {

    private final OptimizationService service;
    public OptimizationController(OptimizationService service) { this.service = service; }

    /**
     * GET /api/optimization/transports?factoryId=1&consumerId=2&productId=3&quantity=500
     */
    @GetMapping("/transports")
    public List<Transport> findSuitableTransports(@RequestParam Long factoryId,
                                                   @RequestParam Long consumerId,
                                                   @RequestParam Long productId,
                                                   @RequestParam Double quantity) {
        return service.findSuitableTransports(factoryId, consumerId, productId, quantity);
    }

    /**
     * GET /api/optimization/distance?factoryId=1&consumerId=2
     */
    @GetMapping("/distance")
    public Map<String, Object> calculateDistance(@RequestParam Long factoryId,
                                                  @RequestParam Long consumerId) {
        return service.calculateDistance(factoryId, consumerId);
    }

    /**
     * GET /api/optimization/plan?factoryId=1&consumerId=2&productId=3&quantity=500
     */
    @GetMapping("/plan")
    public Map<String, Object> planDelivery(@RequestParam Long factoryId,
                                             @RequestParam Long consumerId,
                                             @RequestParam Long productId,
                                             @RequestParam Double quantity) {
        return service.planDelivery(factoryId, consumerId, productId, quantity);
    }
}
