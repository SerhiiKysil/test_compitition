package org.example.backend.entity;

import jakarta.persistence.*;
import org.example.backend.enums.StopType;
import java.time.LocalDateTime;

@Entity
@Table(name = "route_stop")
public class RouteStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "delivery_order_id", nullable = false)
    private DeliveryOrder deliveryOrder;

    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    private Integer stopOrder;

    @Enumerated(EnumType.STRING)
    private StopType stopType;

    private LocalDateTime estimatedArrival;
    private LocalDateTime actualArrival;
    private String notes;

    public RouteStop() {}

    public Long getId() { return id; }
    public DeliveryOrder getDeliveryOrder() { return deliveryOrder; }
    public Location getLocation() { return location; }
    public Integer getStopOrder() { return stopOrder; }
    public StopType getStopType() { return stopType; }
    public LocalDateTime getEstimatedArrival() { return estimatedArrival; }
    public LocalDateTime getActualArrival() { return actualArrival; }
    public String getNotes() { return notes; }

    public void setId(Long id) { this.id = id; }
    public void setDeliveryOrder(DeliveryOrder deliveryOrder) { this.deliveryOrder = deliveryOrder; }
    public void setLocation(Location location) { this.location = location; }
    public void setStopOrder(Integer stopOrder) { this.stopOrder = stopOrder; }
    public void setStopType(StopType stopType) { this.stopType = stopType; }
    public void setEstimatedArrival(LocalDateTime estimatedArrival) { this.estimatedArrival = estimatedArrival; }
    public void setActualArrival(LocalDateTime actualArrival) { this.actualArrival = actualArrival; }
    public void setNotes(String notes) { this.notes = notes; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final RouteStop rs = new RouteStop();
        public Builder deliveryOrder(DeliveryOrder v) { rs.deliveryOrder = v; return this; }
        public Builder location(Location v) { rs.location = v; return this; }
        public Builder stopOrder(Integer v) { rs.stopOrder = v; return this; }
        public Builder stopType(StopType v) { rs.stopType = v; return this; }
        public Builder estimatedArrival(LocalDateTime v) { rs.estimatedArrival = v; return this; }
        public Builder notes(String v) { rs.notes = v; return this; }
        public RouteStop build() { return rs; }
    }
}
