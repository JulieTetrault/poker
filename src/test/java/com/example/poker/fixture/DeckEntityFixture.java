package com.example.poker.fixture;

import com.example.poker.persistence.entity.DeckEntity;
import com.example.poker.persistence.entity.GameEntity;
import com.github.javafaker.Faker;
import java.util.UUID;

public final class DeckEntityFixture {
    private UUID id;
    private GameEntity game;

    public DeckEntityFixture() {
        Faker faker = new Faker();
        id = UUID.fromString(faker.internet().uuid());
        game = new GameEntityFixture().build();
    }

    public DeckEntityFixture withId(UUID id) {
        this.id = id;
        return this;
    }

    public DeckEntityFixture withGame(GameEntity game) {
        this.game = game;
        return this;
    }

    public DeckEntity build() {
        return new DeckEntity(id, game);
    }
}
