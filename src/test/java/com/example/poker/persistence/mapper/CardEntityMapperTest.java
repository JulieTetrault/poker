package com.example.poker.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.poker.domain.model.Card;
import com.example.poker.fixture.CardEntityFixture;
import com.example.poker.fixture.CardFixture;
import com.example.poker.persistence.entity.CardEntity;
import org.junit.jupiter.api.Test;

class CardEntityMapperTest {
    private static final Card SOME_CARD = new CardFixture().build();
    private static final CardEntity SOME_CARD_ENTITY = new CardEntityFixture().build();

    private final CardEntityMapper cardEntityMapper = new CardEntityMapper();

    @Test
    void givenCard__whenMappingToEntity__thenReturnCardEntity() {
        CardEntity cardEntity = cardEntityMapper.toEntity(SOME_CARD);

        assertThat(cardEntity).isInstanceOf(CardEntity.class);
        assertThat(cardEntity.rank()).isEqualTo(SOME_CARD.rank());
        assertThat(cardEntity.suit()).isEqualTo(SOME_CARD.suit());
    }

    @Test
    void givenCardEntity__whenMappingFromEntity__thenReturnCard() {
        Card card = cardEntityMapper.fromEntity(SOME_CARD_ENTITY);

        assertThat(card).isInstanceOf(Card.class);
        assertThat(card.rank()).isEqualTo(SOME_CARD_ENTITY.rank());
        assertThat(card.suit()).isEqualTo(SOME_CARD_ENTITY.suit());
    }
}
