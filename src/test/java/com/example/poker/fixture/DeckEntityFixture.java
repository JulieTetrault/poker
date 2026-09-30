package com.example.poker.fixture;

import com.example.poker.domain.model.Rank;
import com.example.poker.domain.model.Suit;
import com.example.poker.persistence.entity.CardEntity;
import com.example.poker.persistence.entity.DeckEntity;
import com.example.poker.persistence.entity.GameEntity;
import com.github.javafaker.Faker;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class DeckEntityFixture {
    private UUID id;
    private GameEntity game;
    private List<CardEntity> cards = new ArrayList<>();

    public DeckEntityFixture() {
        Faker faker = new Faker();
        id = UUID.fromString(faker.internet().uuid());
        game = new GameEntityFixture().build();
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.add(new CardEntity(suit, rank));
            }
        }
    }

    public DeckEntityFixture withId(UUID id) {
        this.id = id;
        return this;
    }

    public DeckEntityFixture withGame(GameEntity game) {
        this.game = game;
        return this;
    }

    public DeckEntityFixture withCards(List<CardEntity> cards) {
        this.cards = List.copyOf(cards);
        return this;
    }

    public DeckEntity build() {
        return new DeckEntity(id, game, cards);
    }
}
