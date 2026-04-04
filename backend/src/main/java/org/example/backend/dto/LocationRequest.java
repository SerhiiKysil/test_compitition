package org.example.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record LocationRequest(
        @NotBlank String name,
        String address,
        String city,
        String country,
        Double latitude,
        Double longitude,
        boolean hasPort,
        boolean hasAirport,
        boolean hasRailTerminal,
        boolean hasRoadAccess
) {}
