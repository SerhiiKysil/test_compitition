package org.example.backend.entity;

import jakarta.persistence.*;
import org.example.backend.enums.DeliveryStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_order")
public class DeliveryOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String orderNumber;

    @ManyToOne
    @JoinColumn(name = "contract_id")
    private Contract contract;

    @ManyToOne
    @JoinColumn(name = "factory_id")
    private Factory factory;

    @ManyToOne
    @JoinColumn(name = "consumer_id")
    private Consumer consumer;

    @ManyToOne
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    @ManyToOne
    @JoinColumn(name = "transport_id")
    private Transport transport;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private Double quantity;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status = DeliveryStatus.PENDING;

    private LocalDate scheduledPickup;
    private LocalDate scheduledDelivery;
    private LocalDateTime actualPickup;
    private LocalDateTime actualDelivery;
    private String notes;
    private LocalDateTime createdAt;

    public DeliveryOrder() {}

    @PrePersist
    private void prePersist() { createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public String getOrderNumber() { return orderNumber; }
    public Contract getContract() { return contract; }
    public Factory getFactory() { return factory; }
    public Consumer getConsumer() { return consumer; }
    public Warehouse getWarehouse() { return warehouse; }
    public Transport getTransport() { return transport; }
    public Product getProduct() { return product; }
    public Double getQuantity() { return quantity; }
    public DeliveryStatus getStatus() { return status; }
    public LocalDate getScheduledPickup() { return scheduledPickup; }
    public LocalDate getScheduledDelivery() { return scheduledDelivery; }
    public LocalDateTime getActualPickup() { return actualPickup; }
    public LocalDateTime getActualDelivery() { return actualDelivery; }
    public String getNotes() { return notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public void setContract(Contract contract) { this.contract = contract; }
    public void setFactory(Factory factory) { this.factory = factory; }
    public void setConsumer(Consumer consumer) { this.consumer = consumer; }
    public void setWarehouse(Warehouse warehouse) { this.warehouse = warehouse; }
    public void setTransport(Transport transport) { this.transport = transport; }
    public void setProduct(Product product) { this.product = product; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }
    public void setStatus(DeliveryStatus status) { this.status = status; }
    public void setScheduledPickup(LocalDate scheduledPickup) { this.scheduledPickup = scheduledPickup; }
    public void setScheduledDelivery(LocalDate scheduledDelivery) { this.scheduledDelivery = scheduledDelivery; }
    public void setActualPickup(LocalDateTime actualPickup) { this.actualPickup = actualPickup; }
    public void setActualDelivery(LocalDateTime actualDelivery) { this.actualDelivery = actualDelivery; }
    public void setNotes(String notes) { this.notes = notes; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final DeliveryOrder o = new DeliveryOrder();
        public Builder orderNumber(String v) { o.orderNumber = v; return this; }
        public Builder contract(Contract v) { o.contract = v; return this; }
        public Builder factory(Factory v) { o.factory = v; return this; }
        public Builder consumer(Consumer v) { o.consumer = v; return this; }
        public Builder warehouse(Warehouse v) { o.warehouse = v; return this; }
        public Builder transport(Transport v) { o.transport = v; return this; }
        public Builder product(Product v) { o.product = v; return this; }
        public Builder quantity(Double v) { o.quantity = v; return this; }
        public Builder status(DeliveryStatus v) { o.status = v; return this; }
        public Builder scheduledPickup(LocalDate v) { o.scheduledPickup = v; return this; }
        public Builder scheduledDelivery(LocalDate v) { o.scheduledDelivery = v; return this; }
        public Builder notes(String v) { o.notes = v; return this; }
        public DeliveryOrder build() { return o; }
    }
}
