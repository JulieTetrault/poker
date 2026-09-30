package com.example.poker.fixture;

import com.example.poker.persistence.entity.GameEntity;
import com.example.poker.persistence.entity.PlayerEntity;
import com.github.javafaker.Faker;
import java.util.List;
import java.util.UUID;

public final class GameEntityFixture {
    private UUID id;
    private String name;
    private List<PlayerEntity> players = List.of();

    public GameEntityFixture() {
        Faker faker = new Faker();
        id = UUID.fromString(faker.internet().uuid());
        name = faker.lorem().word();
    }

    public GameEntityFixture withId(UUID id) {
        this.id = id;
        return this;
    }

    public GameEntityFixture withName(String name) {
        this.name = name;
        return this;
    }

    public GameEntityFixture withPlayers(List<PlayerEntity> players) {
        this.players = List.copyOf(players);
        return this;
    }

    public GameEntity build() {
        GameEntity gameEntity = new GameEntity(id, name);
        this.players.forEach(gameEntity::addPlayer);
        return gameEntity;
    }
}
