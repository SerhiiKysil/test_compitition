package org.example.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "location")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    private String address;
    private String city;
    private String country;
    private Double latitude;
    private Double longitude;
    private boolean hasPort = false;
    private boolean hasAirport = false;
    private boolean hasRailTerminal = false;
    private boolean hasRoadAccess = true;

    public Location() {}

    // getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public String getCountry() { return country; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public boolean isHasPort() { return hasPort; }
    public boolean isHasAirport() { return hasAirport; }
    public boolean isHasRailTerminal() { return hasRailTerminal; }
    public boolean isHasRoadAccess() { return hasRoadAccess; }

    // setters
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAddress(String address) { this.address = address; }
    public void setCity(String city) { this.city = city; }
    public void setCountry(String country) { this.country = country; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public void setHasPort(boolean hasPort) { this.hasPort = hasPort; }
    public void setHasAirport(boolean hasAirport) { this.hasAirport = hasAirport; }
    public void setHasRailTerminal(boolean hasRailTerminal) { this.hasRailTerminal = hasRailTerminal; }
    public void setHasRoadAccess(boolean hasRoadAccess) { this.hasRoadAccess = hasRoadAccess; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final Location loc = new Location();
        public Builder name(String v) { loc.name = v; return this; }
        public Builder address(String v) { loc.address = v; return this; }
        public Builder city(String v) { loc.city = v; return this; }
        public Builder country(String v) { loc.country = v; return this; }
        public Builder latitude(Double v) { loc.latitude = v; return this; }
        public Builder longitude(Double v) { loc.longitude = v; return this; }
        public Builder hasPort(boolean v) { loc.hasPort = v; return this; }
        public Builder hasAirport(boolean v) { loc.hasAirport = v; return this; }
        public Builder hasRailTerminal(boolean v) { loc.hasRailTerminal = v; return this; }
        public Builder hasRoadAccess(boolean v) { loc.hasRoadAccess = v; return this; }
        public Location build() { return loc; }
    }
}
