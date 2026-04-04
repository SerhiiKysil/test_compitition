package org.example.backend.repository;

import org.example.backend.entity.WarehouseProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseProductRepository extends JpaRepository<WarehouseProduct, Long> {

    List<WarehouseProduct> findByWarehouseId(Long warehouseId);

    List<WarehouseProduct> findByProductId(Long productId);

    Optional<WarehouseProduct> findByWarehouseIdAndProductId(Long warehouseId, Long productId);
}
