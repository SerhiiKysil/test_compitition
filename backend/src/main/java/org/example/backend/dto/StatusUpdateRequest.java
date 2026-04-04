package org.example.backend.dto;

import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(@NotNull String status) {}
