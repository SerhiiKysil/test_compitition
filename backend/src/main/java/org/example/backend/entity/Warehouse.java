package org.example.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "warehouse")
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;

    private Double maxCapacity;
    private Double currentLoad = 0.0;
    private String description;
    private String contactPhone;
    private LocalDateTime createdAt;

    public Warehouse() {}

    @PrePersist
    private void prePersist() { createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public String getName() { return name; }
    public Location getLocation() { return location; }
    public Double getMaxCapacity() { return maxCapacity; }
    public Double getCurrentLoad() { return currentLoad; }
    public String getDescription() { return description; }
    public String getContactPhone() { return contactPhone; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setLocation(Location location) { this.location = location; }
    public void setMaxCapacity(Double maxCapacity) { this.maxCapacity = maxCapacity; }
    public void setCurrentLoad(Double currentLoad) { this.currentLoad = currentLoad; }
    public void setDescription(String description) { this.description = description; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final Warehouse w = new Warehouse();
        public Builder name(String v) { w.name = v; return this; }
        public Builder location(Location v) { w.location = v; return this; }
        public Builder maxCapacity(Double v) { w.maxCapacity = v; return this; }
        public Builder currentLoad(Double v) { w.currentLoad = v; return this; }
        public Builder description(String v) { w.description = v; return this; }
        public Builder contactPhone(String v) { w.contactPhone = v; return this; }
        public Warehouse build() { return w; }
    }
}
