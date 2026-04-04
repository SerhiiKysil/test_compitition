package org.example.backend.service;

import org.example.backend.dto.WarehouseProductRequest;
import org.example.backend.dto.WarehouseRequest;
import org.example.backend.entity.*;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseProductRepository warehouseProductRepository;
    private final LocationRepository locationRepository;
    private final ProductRepository productRepository;

    public WarehouseService(WarehouseRepository warehouseRepository,
                            WarehouseProductRepository warehouseProductRepository,
                            LocationRepository locationRepository,
                            ProductRepository productRepository) {
        this.warehouseRepository = warehouseRepository;
        this.warehouseProductRepository = warehouseProductRepository;
        this.locationRepository = locationRepository;
        this.productRepository = productRepository;
    }

    public List<Warehouse> getAll() { return warehouseRepository.findAll(); }

    public Warehouse getById(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", id));
    }

    public List<WarehouseProduct> getProducts(Long warehouseId) {
        getById(warehouseId);
        return warehouseProductRepository.findByWarehouseId(warehouseId);
    }

    @Transactional
    public Warehouse create(WarehouseRequest req) {
        Location loc = locationRepository.findById(req.locationId())
                .orElseThrow(() -> new ResourceNotFoundException("Location", req.locationId()));
        return warehouseRepository.save(Warehouse.builder()
                .name(req.name()).location(loc).maxCapacity(req.maxCapacity())
                .currentLoad(0.0).description(req.description()).contactPhone(req.contactPhone())
                .build());
    }

    @Transactional
    public Warehouse update(Long id, WarehouseRequest req) {
        Warehouse w = getById(id);
        Location loc = locationRepository.findById(req.locationId())
                .orElseThrow(() -> new ResourceNotFoundException("Location", req.locationId()));
        w.setName(req.name()); w.setLocation(loc);
        w.setMaxCapacity(req.maxCapacity()); w.setDescription(req.description());
        w.setContactPhone(req.contactPhone());
        return warehouseRepository.save(w);
    }

    @Transactional
    public WarehouseProduct addOrUpdateProduct(Long warehouseId, WarehouseProductRequest req) {
        Warehouse warehouse = getById(warehouseId);
        Product product = productRepository.findById(req.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", req.productId()));
        WarehouseProduct wp = warehouseProductRepository
                .findByWarehouseIdAndProductId(warehouseId, req.productId())
                .orElse(WarehouseProduct.builder().warehouse(warehouse).product(product).build());
        wp.setQuantity(req.quantity());
        return warehouseProductRepository.save(wp);
    }

    @Transactional
    public void removeProduct(Long warehouseId, Long productId) {
        WarehouseProduct wp = warehouseProductRepository
                .findByWarehouseIdAndProductId(warehouseId, productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "WarehouseProduct for warehouse " + warehouseId + " / product " + productId));
        warehouseProductRepository.delete(wp);
    }

    @Transactional
    public void delete(Long id) {
        getById(id);
        warehouseRepository.deleteById(id);
    }
}
