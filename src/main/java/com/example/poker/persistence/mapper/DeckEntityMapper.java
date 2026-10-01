package com.example.poker.persistence.mapper;

import com.example.poker.domain.model.Deck;
import com.example.poker.persistence.entity.DeckEntity;
import com.example.poker.persistence.entity.GameEntity;
import org.springframework.stereotype.Component;

@Component
public final class DeckEntityMapper {
    public DeckEntity toEntity(Deck deck) {
        return toEntity(deck, null);
    }

    public DeckEntity toEntity(Deck deck, GameEntity game) {
        return new DeckEntity(deck.getId(), game);
    }

    public Deck fromEntity(DeckEntity entity) {
        return new Deck(
                entity.getId(),
                entity.getGame() == null ? null : entity.getGame().getId());
    }
}
