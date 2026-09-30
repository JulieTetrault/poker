package com.example.poker.domain.factory;

import com.example.poker.domain.model.Card;
import com.example.poker.domain.model.Deck;
import com.example.poker.domain.model.Rank;
import com.example.poker.domain.model.Suit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class DeckFactory {
    private final IdGenerator idGenerator;

    public DeckFactory(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public Deck create() {
        UUID id = idGenerator.nextId();
        List<Card> cards = generateCards(id);
        return new Deck(id, cards);
    }

    private List<Card> generateCards(UUID deckId) {
        List<Card> cards = new ArrayList<>();

        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.add(new Card(UUID.randomUUID(), deckId, suit, rank));
            }
        }

        return cards;
    }
}
