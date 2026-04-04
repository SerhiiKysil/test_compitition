package org.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FactoryRequest(
        @NotBlank String name,
        @NotNull Long locationId,
        String description,
        String contactName,
        String contactPhone,
        String contactEmail,
        Double productionCapacity
) {}
