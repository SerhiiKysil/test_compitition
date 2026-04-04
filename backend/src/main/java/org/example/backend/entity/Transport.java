package org.example.backend.entity;

import jakarta.persistence.*;
import org.example.backend.enums.TransportStatus;
import org.example.backend.enums.TransportType;
import java.time.LocalDateTime;

@Entity
@Table(name = "transport")
public class Transport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransportType type;

    private Double maxCargoWeightTons;
    private Double maxCargoVolumeM3;
    private String fuelType;
    private Double fuelCapacityLiters;
    private Double rangeKm;

    @Enumerated(EnumType.STRING)
    private TransportStatus status = TransportStatus.AVAILABLE;

    @ManyToOne
    @JoinColumn(name = "current_location_id")
    private Location currentLocation;

    private String description;
    private LocalDateTime createdAt;

    public Transport() {}

    @PrePersist
    private void prePersist() { createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public String getName() { return name; }
    public TransportType getType() { return type; }
    public Double getMaxCargoWeightTons() { return maxCargoWeightTons; }
    public Double getMaxCargoVolumeM3() { return maxCargoVolumeM3; }
    public String getFuelType() { return fuelType; }
    public Double getFuelCapacityLiters() { return fuelCapacityLiters; }
    public Double getRangeKm() { return rangeKm; }
    public TransportStatus getStatus() { return status; }
    public Location getCurrentLocation() { return currentLocation; }
    public String getDescription() { return description; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setType(TransportType type) { this.type = type; }
    public void setMaxCargoWeightTons(Double maxCargoWeightTons) { this.maxCargoWeightTons = maxCargoWeightTons; }
    public void setMaxCargoVolumeM3(Double maxCargoVolumeM3) { this.maxCargoVolumeM3 = maxCargoVolumeM3; }
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }
    public void setFuelCapacityLiters(Double fuelCapacityLiters) { this.fuelCapacityLiters = fuelCapacityLiters; }
    public void setRangeKm(Double rangeKm) { this.rangeKm = rangeKm; }
    public void setStatus(TransportStatus status) { this.status = status; }
    public void setCurrentLocation(Location currentLocation) { this.currentLocation = currentLocation; }
    public void setDescription(String description) { this.description = description; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final Transport t = new Transport();
        public Builder name(String v) { t.name = v; return this; }
        public Builder type(TransportType v) { t.type = v; return this; }
        public Builder maxCargoWeightTons(Double v) { t.maxCargoWeightTons = v; return this; }
        public Builder maxCargoVolumeM3(Double v) { t.maxCargoVolumeM3 = v; return this; }
        public Builder fuelType(String v) { t.fuelType = v; return this; }
        public Builder fuelCapacityLiters(Double v) { t.fuelCapacityLiters = v; return this; }
        public Builder rangeKm(Double v) { t.rangeKm = v; return this; }
        public Builder status(TransportStatus v) { t.status = v; return this; }
        public Builder currentLocation(Location v) { t.currentLocation = v; return this; }
        public Builder description(String v) { t.description = v; return this; }
        public Transport build() { return t; }
    }
}
