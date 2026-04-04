package org.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.backend.enums.ServiceStationType;

import java.math.BigDecimal;

public record ServiceStationRequest(
        @NotBlank String name,
        @NotNull Long locationId,
        @NotNull ServiceStationType type,
        String fuelTypes,
        BigDecimal pricePerLiter,
        Boolean hasHeavyLift,
        Double maxRepairWeightTons,
        String description,
        String contactPhone
) {}
