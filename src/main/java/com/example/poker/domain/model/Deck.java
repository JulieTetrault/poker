package com.example.poker.domain.model;

import java.util.List;
import java.util.UUID;

public final class Deck {
    private final UUID id;
    private UUID gameId;
    private final List<Card> cards;

    public Deck(UUID id, List<Card> cards) {
        this(id, null, cards);
    }

    public Deck(UUID id, UUID gameId, List<Card> cards) {
        this.id = id;
        this.gameId = gameId;
        this.cards = cards;
    }

    public UUID getId() {
        return id;
    }

    public UUID getGameId() {
        return gameId;
    }

    public List<Card> getCards() {
        return cards;
    }

    public void setGameId(UUID gameId) {
        this.gameId = gameId;
    }
}
