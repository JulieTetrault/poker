package com.example.poker.fixture;

import com.example.poker.domain.model.Deck;
import com.example.poker.domain.model.Game;
import com.example.poker.domain.model.Player;
import com.github.javafaker.Faker;
import java.util.List;
import java.util.UUID;

public final class GameFixture {
    private UUID id;
    private String name;
    private List<Deck> decks = List.of();
    private List<Player> players = List.of();

    public GameFixture() {
        Faker faker = new Faker();
        id = UUID.fromString(faker.internet().uuid());
        name = faker.lorem().word();
    }

    public GameFixture withId(UUID id) {
        this.id = id;
        return this;
    }

    public GameFixture withName(String name) {
        this.name = name;
        return this;
    }

    public GameFixture withDecks(List<Deck> decks) {
        this.decks = List.copyOf(decks);
        return this;
    }

    public GameFixture withPlayers(List<Player> players) {
        this.players = List.copyOf(players);
        return this;
    }

    public Game build() {
        Game game = new Game(id, name);
        decks.forEach(game::addDeck);
        players.forEach(game::addPlayer);
        return game;
    }
}
