package com.example.poker.fixture;

import com.example.poker.domain.model.Deck;
import com.example.poker.domain.model.Shoe;
import java.util.List;

public final class ShoeFixture {
    private List<Deck> decks = List.of();

    public ShoeFixture() {}

    public ShoeFixture withDecks(List<Deck> decks) {
        this.decks = List.copyOf(decks);
        return this;
    }

    public Shoe build() {
        Shoe shoe = new Shoe();
        shoe.getDecks().addAll(decks);
        return shoe;
    }
}
