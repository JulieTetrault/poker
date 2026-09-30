package com.example.poker.fixture;

import com.example.poker.domain.model.Card;
import com.example.poker.domain.model.Rank;
import com.example.poker.domain.model.Suit;
import com.github.javafaker.Faker;
import java.util.UUID;

public final class CardFixture {
    private UUID id;
    private UUID deckId;
    private Suit suit;
    private Rank rank;

    public CardFixture() {
        Faker faker = new Faker();
        id = UUID.fromString(faker.internet().uuid());
        deckId = UUID.fromString(faker.internet().uuid());
        suit = faker.options().option(Suit.class);
        rank = faker.options().option(Rank.class);
    }

    public CardFixture withId(UUID id) {
        this.id = id;
        return this;
    }

    public CardFixture withDeckId(UUID deckId) {
        this.deckId = deckId;
        return this;
    }

    public CardFixture withSuit(Suit suit) {
        this.suit = suit;
        return this;
    }

    public CardFixture withRank(Rank rank) {
        this.rank = rank;
        return this;
    }

    public Card build() {
        return new Card(id, deckId, suit, rank);
    }
}
