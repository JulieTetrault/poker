package com.example.poker.fixture;

import com.example.poker.domain.model.Card;
import com.example.poker.domain.model.Player;
import com.github.javafaker.Faker;
import java.util.List;
import java.util.UUID;

public final class PlayerFixture {
    private UUID id;
    private UUID gameId;
    private String name;
    private List<Card> cards = List.of();

    public PlayerFixture() {
        Faker faker = new Faker();
        id = UUID.fromString(faker.internet().uuid());
        gameId = UUID.fromString(faker.internet().uuid());
        name = faker.name().fullName();
    }

    public PlayerFixture withId(UUID id) {
        this.id = id;
        return this;
    }

    public PlayerFixture withGameId(UUID gameId) {
        this.gameId = gameId;
        return this;
    }

    public PlayerFixture withName(String name) {
        this.name = name;
        return this;
    }

    public PlayerFixture withCards(List<Card> cards) {
        this.cards = List.copyOf(cards);
        return this;
    }

    public Player build() {
        Player player = new Player(id, gameId, name);
        player.addCards(cards);
        return player;
    }
}
