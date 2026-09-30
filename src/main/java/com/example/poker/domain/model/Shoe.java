package com.example.poker.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Shoe {
    private final UUID id;
    private final UUID gameId;
    private final List<Deck> decks = new ArrayList<>();

    public Shoe(UUID id, UUID gameId) {
        this.id = id;
        this.gameId = gameId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getGameId() {
        return gameId;
    }

    public List<Deck> getDecks() {
        return decks;
    }
}
