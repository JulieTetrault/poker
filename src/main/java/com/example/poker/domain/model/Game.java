package com.example.poker.domain.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class Game {
    private final UUID id;
    private final String name;
    private final Shoe shoe;
    private final Map<UUID, Player> players;

    public Game(UUID id, String name) {
        this(id, name, new Shoe(), new HashMap<>());
    }

    public Game(UUID id, String name, Shoe shoe, Map<UUID, Player> players) {
        this.id = id;
        this.name = name;
        this.shoe = shoe;
        this.players = new HashMap<>(players);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Shoe getShoe() {
        return shoe;
    }

    public List<Player> getPlayers() {
        return List.copyOf(players.values());
    }

    public void addPlayer(Player player) {
        players.put(player.getId(), player);
    }

    public void removePlayer(Player player) {
        players.remove(player.getId());
    }

    public void addDeck(Deck deck) {
        shoe.addDeck(deck);
    }

    public List<Card> dealCards(int cardCount, Player player) {
        List<Card> dealt = shoe.dealCards(cardCount);
        player.addCards(dealt);
        players.replace(player.getId(), player);
        return dealt;
    }
}
