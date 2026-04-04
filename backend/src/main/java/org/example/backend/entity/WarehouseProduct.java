package org.example.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "warehouse_product",
        uniqueConstraints = @UniqueConstraint(columnNames = {"warehouse_id", "product_id"}))
public class WarehouseProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private Double quantity = 0.0;

    public WarehouseProduct() {}

    public Long getId() { return id; }
    public Warehouse getWarehouse() { return warehouse; }
    public Product getProduct() { return product; }
    public Double getQuantity() { return quantity; }

    public void setId(Long id) { this.id = id; }
    public void setWarehouse(Warehouse warehouse) { this.warehouse = warehouse; }
    public void setProduct(Product product) { this.product = product; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final WarehouseProduct wp = new WarehouseProduct();
        public Builder warehouse(Warehouse v) { wp.warehouse = v; return this; }
        public Builder product(Product v) { wp.product = v; return this; }
        public Builder quantity(Double v) { wp.quantity = v; return this; }
        public WarehouseProduct build() { return wp; }
    }
}
