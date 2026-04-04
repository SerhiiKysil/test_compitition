package org.example.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    private String category;
    private Double weightPerUnit;
    private Double volumePerUnit;
    private String unit;

    public Product() {}

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public Double getWeightPerUnit() { return weightPerUnit; }
    public Double getVolumePerUnit() { return volumePerUnit; }
    public String getUnit() { return unit; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setCategory(String category) { this.category = category; }
    public void setWeightPerUnit(Double weightPerUnit) { this.weightPerUnit = weightPerUnit; }
    public void setVolumePerUnit(Double volumePerUnit) { this.volumePerUnit = volumePerUnit; }
    public void setUnit(String unit) { this.unit = unit; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final Product p = new Product();
        public Builder name(String v) { p.name = v; return this; }
        public Builder category(String v) { p.category = v; return this; }
        public Builder weightPerUnit(Double v) { p.weightPerUnit = v; return this; }
        public Builder volumePerUnit(Double v) { p.volumePerUnit = v; return this; }
        public Builder unit(String v) { p.unit = v; return this; }
        public Product build() { return p; }
    }
}
