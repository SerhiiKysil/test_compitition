package org.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ConsumerRequest(
        @NotBlank String name,
        @NotNull Long locationId,
        String contactName,
        String contactPhone,
        String contactEmail,
        String description
) {}
