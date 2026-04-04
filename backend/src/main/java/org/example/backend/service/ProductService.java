package org.example.backend.service;

import org.example.backend.dto.ProductRequest;
import org.example.backend.entity.Product;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public List<Product> getAll() {
        return repository.findAll();
    }

    public Product getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    @Transactional
    public Product create(ProductRequest req) {
        return repository.save(Product.builder()
                .name(req.name()).category(req.category())
                .weightPerUnit(req.weightPerUnit()).volumePerUnit(req.volumePerUnit())
                .unit(req.unit()).build());
    }

    @Transactional
    public Product update(Long id, ProductRequest req) {
        Product p = getById(id);
        p.setName(req.name()); p.setCategory(req.category());
        p.setWeightPerUnit(req.weightPerUnit()); p.setVolumePerUnit(req.volumePerUnit());
        p.setUnit(req.unit());
        return repository.save(p);
    }

    @Transactional
    public void delete(Long id) {
        getById(id);
        repository.deleteById(id);
    }
}
