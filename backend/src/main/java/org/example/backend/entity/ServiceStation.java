package org.example.backend.entity;

import jakarta.persistence.*;
import org.example.backend.enums.ServiceStationType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "service_station")
public class ServiceStation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceStationType type;

    private String fuelTypes;
    private BigDecimal pricePerLiter;
    private Boolean hasHeavyLift = false;
    private Double maxRepairWeightTons;
    private String description;
    private String contactPhone;
    private LocalDateTime createdAt;

    public ServiceStation() {}

    @PrePersist
    private void prePersist() { createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public String getName() { return name; }
    public Location getLocation() { return location; }
    public ServiceStationType getType() { return type; }
    public String getFuelTypes() { return fuelTypes; }
    public BigDecimal getPricePerLiter() { return pricePerLiter; }
    public Boolean getHasHeavyLift() { return hasHeavyLift; }
    public Double getMaxRepairWeightTons() { return maxRepairWeightTons; }
    public String getDescription() { return description; }
    public String getContactPhone() { return contactPhone; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setLocation(Location location) { this.location = location; }
    public void setType(ServiceStationType type) { this.type = type; }
    public void setFuelTypes(String fuelTypes) { this.fuelTypes = fuelTypes; }
    public void setPricePerLiter(BigDecimal pricePerLiter) { this.pricePerLiter = pricePerLiter; }
    public void setHasHeavyLift(Boolean hasHeavyLift) { this.hasHeavyLift = hasHeavyLift; }
    public void setMaxRepairWeightTons(Double maxRepairWeightTons) { this.maxRepairWeightTons = maxRepairWeightTons; }
    public void setDescription(String description) { this.description = description; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final ServiceStation s = new ServiceStation();
        public Builder name(String v) { s.name = v; return this; }
        public Builder location(Location v) { s.location = v; return this; }
        public Builder type(ServiceStationType v) { s.type = v; return this; }
        public Builder fuelTypes(String v) { s.fuelTypes = v; return this; }
        public Builder pricePerLiter(BigDecimal v) { s.pricePerLiter = v; return this; }
        public Builder hasHeavyLift(Boolean v) { s.hasHeavyLift = v; return this; }
        public Builder maxRepairWeightTons(Double v) { s.maxRepairWeightTons = v; return this; }
        public Builder description(String v) { s.description = v; return this; }
        public Builder contactPhone(String v) { s.contactPhone = v; return this; }
        public ServiceStation build() { return s; }
    }
}
