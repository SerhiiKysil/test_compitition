package org.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record DeliveryOrderRequest(
        @NotBlank String orderNumber,
        Long contractId,
        @NotNull Long factoryId,
        @NotNull Long consumerId,
        Long warehouseId,
        Long transportId,
        @NotNull Long productId,
        @NotNull Double quantity,
        LocalDate scheduledPickup,
        LocalDate scheduledDelivery,
        String notes
) {}
