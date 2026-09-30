package com.example.poker.fixture;

import com.example.poker.domain.model.Card;
import com.example.poker.domain.model.Deck;
import com.example.poker.domain.model.Rank;
import com.example.poker.domain.model.Suit;
import com.github.javafaker.Faker;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class DeckFixture {
    private UUID id;
    private UUID gameId;
    private List<Card> cards = new ArrayList<>();

    public DeckFixture() {
        Faker faker = new Faker();
        id = UUID.fromString(faker.internet().uuid());
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.add(new Card(suit, rank));
            }
        }
    }

    public DeckFixture withId(UUID id) {
        this.id = id;
        return this;
    }

    public DeckFixture withGameId(UUID gameId) {
        this.gameId = gameId;
        return this;
    }

    public DeckFixture withCards(List<Card> cards) {
        this.cards = List.copyOf(cards);
        return this;
    }

    public Deck build() {
        Deck deck = new Deck(id, new ArrayList<>(cards));
        deck.setGameId(gameId);
        return deck;
    }
}
