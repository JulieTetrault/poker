package com.example.poker.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.example.poker.domain.model.Card;
import com.example.poker.domain.model.Deck;
import com.example.poker.fixture.CardEntityFixture;
import com.example.poker.fixture.CardFixture;
import com.example.poker.fixture.DeckEntityFixture;
import com.example.poker.fixture.DeckFixture;
import com.example.poker.fixture.GameEntityFixture;
import com.example.poker.persistence.entity.CardEntity;
import com.example.poker.persistence.entity.DeckEntity;
import com.example.poker.persistence.entity.GameEntity;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeckEntityMapperTest {
    private static final GameEntity SOME_GAME_ENTITY = new GameEntityFixture().build();
    private static final Card SOME_CARD = new CardFixture().build();
    private static final CardEntity SOME_CARD_ENTITY = new CardEntityFixture().build();
    private static final Deck SOME_DECK = new DeckFixture().withCards(List.of(SOME_CARD)).build();
    private static final DeckEntity SOME_DECK_ENTITY =
            new DeckEntityFixture().withCards(List.of(SOME_CARD_ENTITY)).build();

    @Mock private CardEntityMapper cardEntityMapper;

    @InjectMocks private DeckEntityMapper deckEntityMapper;

    @Test
    void givenDeck__whenMappingToEntity__thenReturnDeckEntity() {
        given(cardEntityMapper.toEntity(SOME_CARD)).willReturn(SOME_CARD_ENTITY);

        DeckEntity deckEntity = deckEntityMapper.toEntity(SOME_DECK, SOME_GAME_ENTITY);

        assertThat(deckEntity).isInstanceOf(DeckEntity.class);
        assertThat(deckEntity.getId()).isEqualTo(SOME_DECK.getId());
        assertThat(deckEntity.getGame()).isEqualTo(SOME_GAME_ENTITY);
        assertThat(deckEntity.getCards()).containsExactly(SOME_CARD_ENTITY);
    }

    @Test
    void givenDeckEntity__whenMappingFromEntity__thenReturnDeck() {
        given(cardEntityMapper.fromEntity(SOME_CARD_ENTITY)).willReturn(SOME_CARD);

        Deck deck = deckEntityMapper.fromEntity(SOME_DECK_ENTITY);

        assertThat(deck).isInstanceOf(Deck.class);
        assertThat(deck.getId()).isEqualTo(SOME_DECK_ENTITY.getId());
        assertThat(deck.getGameId()).isEqualTo(SOME_DECK_ENTITY.getGame().getId());
        assertThat(deck.getCards()).containsExactly(SOME_CARD);
    }
}
