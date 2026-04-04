package org.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.backend.enums.ContractStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContractRequest(
        @NotBlank String contractNumber,
        @NotNull Long factoryId,
        @NotNull Long consumerId,
        @NotNull Long productId,
        @NotNull Double quantity,
        BigDecimal unitPrice,
        BigDecimal totalValue,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        ContractStatus status,
        String description
) {}
