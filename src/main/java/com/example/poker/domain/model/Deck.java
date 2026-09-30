package com.example.poker.domain.model;

import com.example.poker.domain.exception.DeckAlreadyAttachedException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Deck {
    private final UUID id;
    private UUID gameId;

    public Deck(UUID id) {
        this(id, null);
    }

    public Deck(UUID id, UUID gameId) {
        this.id = id;
        this.gameId = gameId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getGameId() {
        return gameId;
    }

    public List<Card> generateCards() {
        List<Card> cards = new ArrayList<>();
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.add(new Card(suit, rank));
            }
        }
        return cards;
    }

    public void setGameId(UUID gameId) {
        if (this.gameId != null) {
            throw new DeckAlreadyAttachedException(id);
        }
        this.gameId = gameId;
    }
}
