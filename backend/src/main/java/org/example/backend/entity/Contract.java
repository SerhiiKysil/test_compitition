package org.example.backend.entity;

import jakarta.persistence.*;
import org.example.backend.enums.ContractStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contract")
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String contractNumber;

    @ManyToOne
    @JoinColumn(name = "factory_id")
    private Factory factory;

    @ManyToOne
    @JoinColumn(name = "consumer_id")
    private Consumer consumer;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private Double quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalValue;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private ContractStatus status = ContractStatus.ACTIVE;

    private String description;
    private LocalDateTime createdAt;

    public Contract() {}

    @PrePersist
    private void prePersist() { createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public String getContractNumber() { return contractNumber; }
    public Factory getFactory() { return factory; }
    public Consumer getConsumer() { return consumer; }
    public Product getProduct() { return product; }
    public Double getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getTotalValue() { return totalValue; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public ContractStatus getStatus() { return status; }
    public String getDescription() { return description; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setContractNumber(String contractNumber) { this.contractNumber = contractNumber; }
    public void setFactory(Factory factory) { this.factory = factory; }
    public void setConsumer(Consumer consumer) { this.consumer = consumer; }
    public void setProduct(Product product) { this.product = product; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public void setTotalValue(BigDecimal totalValue) { this.totalValue = totalValue; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public void setStatus(ContractStatus status) { this.status = status; }
    public void setDescription(String description) { this.description = description; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final Contract c = new Contract();
        public Builder contractNumber(String v) { c.contractNumber = v; return this; }
        public Builder factory(Factory v) { c.factory = v; return this; }
        public Builder consumer(Consumer v) { c.consumer = v; return this; }
        public Builder product(Product v) { c.product = v; return this; }
        public Builder quantity(Double v) { c.quantity = v; return this; }
        public Builder unitPrice(BigDecimal v) { c.unitPrice = v; return this; }
        public Builder totalValue(BigDecimal v) { c.totalValue = v; return this; }
        public Builder startDate(LocalDate v) { c.startDate = v; return this; }
        public Builder endDate(LocalDate v) { c.endDate = v; return this; }
        public Builder status(ContractStatus v) { c.status = v; return this; }
        public Builder description(String v) { c.description = v; return this; }
        public Contract build() { return c; }
    }
}
