package com.example.poker.domain.model;

import java.util.UUID;

public record Card(UUID id, UUID deckId, Suit suit, Rank rank) {}
