package com.example.poker.fixture;

import com.example.poker.domain.model.Deck;
import com.example.poker.domain.model.Shoe;
import com.github.javafaker.Faker;
import java.util.List;
import java.util.UUID;

public final class ShoeFixture {
    private UUID id;
    private UUID gameId;
    private List<Deck> decks = List.of();

    public ShoeFixture() {
        Faker faker = new Faker();
        id = UUID.fromString(faker.internet().uuid());
        gameId = UUID.fromString(faker.internet().uuid());
    }

    public ShoeFixture withId(UUID id) {
        this.id = id;
        return this;
    }

    public ShoeFixture withGameId(UUID gameId) {
        this.gameId = gameId;
        return this;
    }

    public ShoeFixture withDecks(List<Deck> decks) {
        this.decks = List.copyOf(decks);
        return this;
    }

    public Shoe build() {
        Shoe shoe = new Shoe(id, gameId);
        shoe.getDecks().addAll(decks);
        return shoe;
    }
}
