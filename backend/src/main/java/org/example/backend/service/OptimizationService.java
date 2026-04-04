package org.example.backend.service;

import org.example.backend.entity.Factory;
import org.example.backend.entity.Location;
import org.example.backend.entity.Product;
import org.example.backend.entity.Transport;
import org.example.backend.enums.TransportStatus;
import org.example.backend.enums.TransportType;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.repository.ConsumerRepository;
import org.example.backend.repository.FactoryRepository;
import org.example.backend.repository.ProductRepository;
import org.example.backend.repository.TransportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class OptimizationService {

    private final TransportRepository transportRepository;
    private final FactoryRepository factoryRepository;
    private final ConsumerRepository consumerRepository;
    private final ProductRepository productRepository;

    public OptimizationService(TransportRepository transportRepository,
                               FactoryRepository factoryRepository,
                               ConsumerRepository consumerRepository,
                               ProductRepository productRepository) {
        this.transportRepository = transportRepository;
        this.factoryRepository = factoryRepository;
        this.consumerRepository = consumerRepository;
        this.productRepository = productRepository;
    }

    /**
     * Знаходить доступний транспорт, сумісний з інфраструктурою обох локацій
     * і здатний підняти потрібну вагу вантажу.
     */
    public List<Transport> findSuitableTransports(Long factoryId, Long consumerId,
                                                   Long productId, Double quantity) {
        Factory factory = factoryRepository.findById(factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Factory", factoryId));
        var consumer = consumerRepository.findById(consumerId)
                .orElseThrow(() -> new ResourceNotFoundException("Consumer", consumerId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

        double totalWeightTons = (product.getWeightPerUnit() != null && quantity != null)
                ? (product.getWeightPerUnit() * quantity) / 1000.0
                : 0.0;

        Location from = factory.getLocation();
        Location to = consumer.getLocation();

        return transportRepository.findByStatus(TransportStatus.AVAILABLE).stream()
                .filter(t -> isCompatibleWithLocations(t.getType(), from, to))
                .filter(t -> t.getMaxCargoWeightTons() == null || t.getMaxCargoWeightTons() >= totalWeightTons)
                .sorted(Comparator.comparingDouble(t ->
                        -(t.getMaxCargoWeightTons() != null ? t.getMaxCargoWeightTons() : 0)))
                .collect(Collectors.toList());
    }

    /** Відстань між фабрикою та споживачем за формулою Гаверсіна (км). */
    public Map<String, Object> calculateDistance(Long factoryId, Long consumerId) {
        Factory factory = factoryRepository.findById(factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Factory", factoryId));
        var consumer = consumerRepository.findById(consumerId)
                .orElseThrow(() -> new ResourceNotFoundException("Consumer", consumerId));

        Location from = factory.getLocation();
        Location to = consumer.getLocation();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("from", from != null ? from.getName() : null);
        result.put("to", to != null ? to.getName() : null);

        if (from == null || to == null
                || from.getLatitude() == null || to.getLatitude() == null) {
            result.put("distanceKm", null);
            result.put("note", "Coordinates not available for one or both locations");
        } else {
            double dist = haversineKm(from.getLatitude(), from.getLongitude(),
                    to.getLatitude(), to.getLongitude());
            result.put("distanceKm", Math.round(dist * 10.0) / 10.0);
        }
        return result;
    }

    /** Повний план доставки: відстань + рекомендований транспорт. */
    public Map<String, Object> planDelivery(Long factoryId, Long consumerId,
                                             Long productId, Double quantity) {
        List<Transport> suitable = findSuitableTransports(factoryId, consumerId, productId, quantity);
        Map<String, Object> distance = calculateDistance(factoryId, consumerId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("route", distance);
        result.put("suitableTransports", suitable);
        result.put("recommendedTransport", suitable.isEmpty() ? null : suitable.get(0));
        result.put("totalRequired", Map.of("quantity", quantity, "productId", productId));
        return result;
    }

    // ─── helpers ────────────────────────────────────────────────────────────────

    private boolean isCompatibleWithLocations(TransportType type, Location from, Location to) {
        if (from == null || to == null) return true;
        return switch (type) {
            case SHIP     -> from.isHasPort() && to.isHasPort();
            case AIRPLANE -> from.isHasAirport() && to.isHasAirport();
            case TRAIN    -> from.isHasRailTerminal() && to.isHasRailTerminal();
            case CAR      -> from.isHasRoadAccess() && to.isHasRoadAccess();
        };
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
