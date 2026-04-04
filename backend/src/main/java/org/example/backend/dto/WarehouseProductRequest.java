package org.example.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record WarehouseProductRequest(
        @NotNull Long productId,
        @NotNull @PositiveOrZero Double quantity
) {}
