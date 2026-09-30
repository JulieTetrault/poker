package com.example.poker.domain.model;

import java.util.ArrayList;
import java.util.List;

public final class Shoe {
    private final List<Deck> decks;

    public Shoe() {
        this(new ArrayList<>());
    }

    public Shoe(List<Deck> decks) {
        this.decks = decks;
    }

    public List<Deck> getDecks() {
        return decks;
    }
}
