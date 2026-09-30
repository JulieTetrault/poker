package com.example.poker.domain.model;

import java.util.List;
import java.util.UUID;

public final class Deck {
    private final UUID id;
    private UUID shoeId;
    private final List<Card> cards;

    public Deck(UUID id, List<Card> cards) {
        this.id = id;
        this.cards = cards;
    }

    public UUID getId() {
        return id;
    }

    public UUID getShoeId() {
        return shoeId;
    }

    public List<Card> getCards() {
        return cards;
    }

    public void setShoeId(UUID shoeId) {
        this.shoeId = shoeId;
    }
}
