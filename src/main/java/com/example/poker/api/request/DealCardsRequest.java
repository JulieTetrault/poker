package com.example.poker.api.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DealCardsRequest(
        @NotNull(message = "Field 'count' is required.")
        @Positive(message = "Field 'count' must be a positive integer.")
        Integer count) {}
