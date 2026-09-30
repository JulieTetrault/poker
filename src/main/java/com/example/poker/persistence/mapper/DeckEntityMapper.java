package com.example.poker.persistence.mapper;

import com.example.poker.domain.factory.DeckFactory;
import com.example.poker.domain.model.Deck;
import com.example.poker.persistence.entity.DeckEntity;
import com.example.poker.persistence.entity.GameEntity;
import org.springframework.stereotype.Component;

@Component
public final class DeckEntityMapper {
    private final DeckFactory deckFactory;

    public DeckEntityMapper(DeckFactory deckFactory) {
        this.deckFactory = deckFactory;
    }

    public DeckEntity toEntity(Deck deck) {
        return toEntity(deck, null);
    }

    public DeckEntity toEntity(Deck deck, GameEntity gameEntity) {
        return new DeckEntity(deck.getId(), gameEntity);
    }

    public Deck fromEntity(DeckEntity entity) {
        return deckFactory.create(
                entity.getId(), entity.getGame() == null ? null : entity.getGame().getId());
    }
}
