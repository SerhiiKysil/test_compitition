package org.example.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "factory")
public class Factory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;

    private String description;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private Double productionCapacity;
    private LocalDateTime createdAt;

    public Factory() {}

    @PrePersist
    private void prePersist() { createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public String getName() { return name; }
    public Location getLocation() { return location; }
    public String getDescription() { return description; }
    public String getContactName() { return contactName; }
    public String getContactPhone() { return contactPhone; }
    public String getContactEmail() { return contactEmail; }
    public Double getProductionCapacity() { return productionCapacity; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setLocation(Location location) { this.location = location; }
    public void setDescription(String description) { this.description = description; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public void setProductionCapacity(Double productionCapacity) { this.productionCapacity = productionCapacity; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final Factory f = new Factory();
        public Builder name(String v) { f.name = v; return this; }
        public Builder location(Location v) { f.location = v; return this; }
        public Builder description(String v) { f.description = v; return this; }
        public Builder contactName(String v) { f.contactName = v; return this; }
        public Builder contactPhone(String v) { f.contactPhone = v; return this; }
        public Builder contactEmail(String v) { f.contactEmail = v; return this; }
        public Builder productionCapacity(Double v) { f.productionCapacity = v; return this; }
        public Factory build() { return f; }
    }
}
