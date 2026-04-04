package org.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.backend.enums.TransportStatus;
import org.example.backend.enums.TransportType;

public record TransportRequest(
        @NotBlank String name,
        @NotNull TransportType type,
        Double maxCargoWeightTons,
        Double maxCargoVolumeM3,
        String fuelType,
        Double fuelCapacityLiters,
        Double rangeKm,
        TransportStatus status,
        Long currentLocationId,
        String description
) {}
