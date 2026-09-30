package com.example.poker.fixture;

import com.example.poker.domain.model.Rank;
import com.example.poker.domain.model.Suit;
import com.example.poker.persistence.entity.CardEntity;
import com.github.javafaker.Faker;

public final class CardEntityFixture {
    private Suit suit;
    private Rank rank;

    public CardEntityFixture() {
        Faker faker = new Faker();
        suit = faker.options().option(Suit.class);
        rank = faker.options().option(Rank.class);
    }

    public CardEntityFixture withSuit(Suit suit) {
        this.suit = suit;
        return this;
    }

    public CardEntityFixture withRank(Rank rank) {
        this.rank = rank;
        return this;
    }

    public CardEntity build() {
        return new CardEntity(suit, rank);
    }
}
