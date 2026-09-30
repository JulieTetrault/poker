package com.example.poker.fixture;

import com.example.poker.persistence.entity.CardEntity;
import com.example.poker.persistence.entity.GameEntity;
import com.example.poker.persistence.entity.PlayerEntity;
import com.github.javafaker.Faker;
import java.util.List;
import java.util.UUID;

public final class PlayerEntityFixture {
    private UUID id;
    private GameEntity game;
    private String name;
    private List<CardEntity> cards = List.of();

    public PlayerEntityFixture() {
        Faker faker = new Faker();
        id = UUID.fromString(faker.internet().uuid());
        game = new GameEntityFixture().build();
        name = faker.name().fullName();
    }

    public PlayerEntityFixture withId(UUID id) {
        this.id = id;
        return this;
    }

    public PlayerEntityFixture withGame(GameEntity game) {
        this.game = game;
        return this;
    }

    public PlayerEntityFixture withName(String name) {
        this.name = name;
        return this;
    }

    public PlayerEntityFixture withCards(List<CardEntity> cards) {
        this.cards = List.copyOf(cards);
        return this;
    }

    public PlayerEntity build() {
        return new PlayerEntity(id, game, name, cards);
    }
}
