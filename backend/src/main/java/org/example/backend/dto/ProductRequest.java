package org.example.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record ProductRequest(
        @NotBlank String name,
        String category,
        Double weightPerUnit,
        Double volumePerUnit,
        String unit
) {}
