package com.example.poker.api.request;

import jakarta.validation.constraints.NotNull;

public record AddDeckRequest(
        @NotNull(message = "Field 'deckId' is required.") String deckId) {}
