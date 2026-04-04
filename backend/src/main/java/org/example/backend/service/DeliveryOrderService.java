package org.example.backend.service;

import org.example.backend.dto.DeliveryOrderRequest;
import org.example.backend.dto.RouteStopRequest;
import org.example.backend.entity.*;
import org.example.backend.enums.DeliveryStatus;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DeliveryOrderService {

    private final DeliveryOrderRepository orderRepository;
    private final RouteStopRepository routeStopRepository;
    private final ContractRepository contractRepository;
    private final FactoryRepository factoryRepository;
    private final ConsumerRepository consumerRepository;
    private final WarehouseRepository warehouseRepository;
    private final TransportRepository transportRepository;
    private final ProductRepository productRepository;
    private final LocationRepository locationRepository;

    public DeliveryOrderService(DeliveryOrderRepository orderRepository,
                                RouteStopRepository routeStopRepository,
                                ContractRepository contractRepository,
                                FactoryRepository factoryRepository,
                                ConsumerRepository consumerRepository,
                                WarehouseRepository warehouseRepository,
                                TransportRepository transportRepository,
                                ProductRepository productRepository,
                                LocationRepository locationRepository) {
        this.orderRepository = orderRepository;
        this.routeStopRepository = routeStopRepository;
        this.contractRepository = contractRepository;
        this.factoryRepository = factoryRepository;
        this.consumerRepository = consumerRepository;
        this.warehouseRepository = warehouseRepository;
        this.transportRepository = transportRepository;
        this.productRepository = productRepository;
        this.locationRepository = locationRepository;
    }

    public List<DeliveryOrder> getAll() { return orderRepository.findAll(); }

    public DeliveryOrder getById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryOrder", id));
    }

    public List<RouteStop> getRoute(Long orderId) {
        getById(orderId);
        return routeStopRepository.findByDeliveryOrderIdOrderByStopOrderAsc(orderId);
    }

    @Transactional
    public DeliveryOrder create(DeliveryOrderRequest req) {
        Factory factory = factoryRepository.findById(req.factoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Factory", req.factoryId()));
        Consumer consumer = consumerRepository.findById(req.consumerId())
                .orElseThrow(() -> new ResourceNotFoundException("Consumer", req.consumerId()));
        Product product = productRepository.findById(req.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", req.productId()));

        DeliveryOrder.Builder builder = DeliveryOrder.builder()
                .orderNumber(req.orderNumber()).factory(factory).consumer(consumer)
                .product(product).quantity(req.quantity())
                .scheduledPickup(req.scheduledPickup()).scheduledDelivery(req.scheduledDelivery())
                .notes(req.notes());

        if (req.contractId() != null)
            builder.contract(contractRepository.findById(req.contractId())
                    .orElseThrow(() -> new ResourceNotFoundException("Contract", req.contractId())));
        if (req.warehouseId() != null)
            builder.warehouse(warehouseRepository.findById(req.warehouseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Warehouse", req.warehouseId())));
        if (req.transportId() != null)
            builder.transport(transportRepository.findById(req.transportId())
                    .orElseThrow(() -> new ResourceNotFoundException("Transport", req.transportId())));

        return orderRepository.save(builder.build());
    }

    @Transactional
    public DeliveryOrder update(Long id, DeliveryOrderRequest req) {
        DeliveryOrder o = getById(id);
        o.setOrderNumber(req.orderNumber());
        o.setFactory(factoryRepository.findById(req.factoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Factory", req.factoryId())));
        o.setConsumer(consumerRepository.findById(req.consumerId())
                .orElseThrow(() -> new ResourceNotFoundException("Consumer", req.consumerId())));
        o.setProduct(productRepository.findById(req.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", req.productId())));
        o.setQuantity(req.quantity());
        o.setScheduledPickup(req.scheduledPickup()); o.setScheduledDelivery(req.scheduledDelivery());
        o.setNotes(req.notes());
        if (req.contractId() != null)
            o.setContract(contractRepository.findById(req.contractId())
                    .orElseThrow(() -> new ResourceNotFoundException("Contract", req.contractId())));
        if (req.warehouseId() != null)
            o.setWarehouse(warehouseRepository.findById(req.warehouseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Warehouse", req.warehouseId())));
        if (req.transportId() != null)
            o.setTransport(transportRepository.findById(req.transportId())
                    .orElseThrow(() -> new ResourceNotFoundException("Transport", req.transportId())));
        return orderRepository.save(o);
    }

    @Transactional
    public DeliveryOrder updateStatus(Long id, DeliveryStatus status) {
        DeliveryOrder o = getById(id);
        o.setStatus(status);
        if (status == DeliveryStatus.IN_TRANSIT && o.getActualPickup() == null)
            o.setActualPickup(LocalDateTime.now());
        if (status == DeliveryStatus.DELIVERED && o.getActualDelivery() == null)
            o.setActualDelivery(LocalDateTime.now());
        return orderRepository.save(o);
    }

    @Transactional
    public RouteStop addRouteStop(Long orderId, RouteStopRequest req) {
        DeliveryOrder order = getById(orderId);
        Location location = locationRepository.findById(req.locationId())
                .orElseThrow(() -> new ResourceNotFoundException("Location", req.locationId()));
        return routeStopRepository.save(RouteStop.builder()
                .deliveryOrder(order).location(location).stopOrder(req.stopOrder())
                .stopType(req.stopType()).estimatedArrival(req.estimatedArrival()).notes(req.notes())
                .build());
    }

    @Transactional
    public RouteStop markStopArrived(Long orderId, Long stopId) {
        RouteStop stop = routeStopRepository.findById(stopId)
                .orElseThrow(() -> new ResourceNotFoundException("RouteStop", stopId));
        if (!stop.getDeliveryOrder().getId().equals(orderId))
            throw new IllegalArgumentException("RouteStop does not belong to order " + orderId);
        stop.setActualArrival(LocalDateTime.now());
        return routeStopRepository.save(stop);
    }

    @Transactional
    public void delete(Long id) {
        getById(id);
        orderRepository.deleteById(id);
    }
}
