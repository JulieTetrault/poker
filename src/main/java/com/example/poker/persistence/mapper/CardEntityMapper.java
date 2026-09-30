package com.example.poker.persistence.mapper;

import com.example.poker.domain.model.Card;
import com.example.poker.persistence.entity.CardEntity;
import org.springframework.stereotype.Component;

@Component
public final class CardEntityMapper {

    public CardEntity toEntity(Card card) {
        return new CardEntity(card.suit(), card.rank());
    }

    public Card fromEntity(CardEntity value) {
        return new Card(value.suit(), value.rank());
    }
}
