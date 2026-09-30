package com.example.poker.domain.model;

import java.util.ArrayList;
import java.util.List;

public final class Hand {
    private final List<Card> cards = new ArrayList<>();

    public Hand() {}

    public void addCards(List<Card> cards) {
        this.cards.addAll(cards);
    }

    public List<Card> getCards() {
        return cards;
    }

    public int getValue() {
        return cards.stream().mapToInt(card -> card.rank().getValue()).sum();
    }
}
