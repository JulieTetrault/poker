package com.example.poker.fixture;

import com.example.poker.domain.model.Deck;
import java.util.UUID;

public final class DeckFixture {
    private UUID id;
    private UUID gameId;

    public DeckFixture() {
        id = UUID.randomUUID();
    }

    public DeckFixture withId(UUID id) {
        this.id = id;
        return this;
    }

    public DeckFixture withGameId(UUID gameId) {
        this.gameId = gameId;
        return this;
    }

    public Deck build() {
        return new Deck(id, gameId);
    }
}
