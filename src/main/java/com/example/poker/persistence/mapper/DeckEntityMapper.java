package com.example.poker.persistence.mapper;

import com.example.poker.domain.model.Deck;
import com.example.poker.persistence.entity.DeckEntity;
import com.example.poker.persistence.entity.GameEntity;
import org.springframework.stereotype.Component;

@Component
public final class DeckEntityMapper {
    private final CardEntityMapper cardEntityMapper;

    public DeckEntityMapper(CardEntityMapper cardEntityMapper) {
        this.cardEntityMapper = cardEntityMapper;
    }

    public DeckEntity toEntity(Deck deck, GameEntity gameEntity) {
        return new DeckEntity(
                deck.getId(),
                gameEntity,
                deck.getCards().stream().map(cardEntityMapper::toEntity).toList());
    }

    public Deck fromEntity(DeckEntity entity) {
        return new Deck(
                entity.getId(),
                entity.getGame().getId(),
                entity.getCards().stream().map(cardEntityMapper::fromEntity).toList());
    }
}
