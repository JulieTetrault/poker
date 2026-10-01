package com.example.poker.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateGameRequest(
        @NotNull(message = "Field 'name' is required.") @NotBlank(message = "Field 'name' must be a nonblank string.")
        String name) {}
