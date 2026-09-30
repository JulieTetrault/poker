package com.example.poker.domain.factory;

import com.example.poker.domain.model.Card;
import com.example.poker.domain.model.Deck;
import com.example.poker.domain.model.Rank;
import com.example.poker.domain.model.Suit;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public final class DeckFactory {
    private final IdGenerator idGenerator;

    public DeckFactory(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public Deck create() {
        List<Card> cards = generateCards();
        return new Deck(idGenerator.nextId(), cards);
    }

    private List<Card> generateCards() {
        List<Card> cards = new ArrayList<>();
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.add(new Card(suit, rank));
            }
        }
        return cards;
    }
}
