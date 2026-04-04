package org.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WarehouseRequest(
        @NotBlank String name,
        @NotNull Long locationId,
        Double maxCapacity,
        String description,
        String contactPhone
) {}
