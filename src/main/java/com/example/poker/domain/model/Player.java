package com.example.poker.domain.model;

import java.util.List;
import java.util.UUID;

public final class Player {
    private final UUID id;
    private final UUID gameId;
    private final String name;
    private final Hand hand;

    public Player(UUID id, UUID gameId, String name) {
        this.id = id;
        this.gameId = gameId;
        this.name = name;
        this.hand = new Hand();
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

    public Hand getHand() {
        return hand;
    }

    public void receiveCards(List<Card> cards) {
        hand.addCards(cards);
    }

    public int getHandValue() {
        return hand.getValue();
    }
}
