package com.example.poker.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Player {
    private final UUID id;
    private final UUID gameId;
    private final String name;
    private final List<Card> cards;

    public Player(UUID id, UUID gameId, String name) {
        this(id, gameId, name, new ArrayList<>());
    }

    public Player(UUID id, UUID gameId, String name, List<Card> cards) {
        this.id = id;
        this.gameId = gameId;
        this.name = name;
        this.cards = new ArrayList<>(cards);
    }

    public UUID getId() {
        return id;
    }

    public UUID getGameId() {
        return gameId;
    }

    public String getName() {
        return name;
    }

    public List<Card> getCards() {
        return cards;
    }

    public void addCards(List<Card> cards) {
        this.cards.addAll(cards);
    }

    public int getHandValue() {
        return cards.stream().mapToInt(card -> card.rank().getValue()).sum();
    }
}
