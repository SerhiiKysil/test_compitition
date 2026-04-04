package org.example.backend.dto;

import jakarta.validation.constraints.NotNull;
import org.example.backend.enums.StopType;

import java.time.LocalDateTime;

public record RouteStopRequest(
        @NotNull Long locationId,
        @NotNull Integer stopOrder,
        StopType stopType,
        LocalDateTime estimatedArrival,
        String notes
) {}
