package com.example.poker.domain.model;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class Game {
    private final UUID id;
    private final String name;
    private final Shoe shoe;
    private final Map<UUID, Player> players = new LinkedHashMap<>();

    public Game(UUID id, String name, Shoe shoe) {
        this.id = id;
        this.name = name;
        this.shoe = shoe;
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

    public void removePlayer(UUID playerId) {
        players.remove(playerId);
    }

    public List<Player> getPlayersByHandValue() {
        return players.values().stream()
                .sorted(
                        Comparator.comparingInt(Player::getHandValue)
                                .reversed()
                                .thenComparing(Player::getName))
                .toList();
    }
}
