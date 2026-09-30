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
            new DeckEntityFixture().withGame(null).withCards(List.of(SOME_CARD_ENTITY)).build();
    private static final DeckEntity SOME_DECK_ENTITY_ATTACHED_TO_GAME =
            new DeckEntityFixture()
                    .withGame(SOME_GAME_ENTITY)
                    .withCards(List.of(SOME_CARD_ENTITY))
                    .build();

    @Mock private CardEntityMapper cardEntityMapper;

    @InjectMocks private DeckEntityMapper deckEntityMapper;

    @Test
    void givenDeckAttachedToGame__whenMappingToEntity__thenReturnDeckEntityAttachedToGame() {
        given(cardEntityMapper.toEntity(SOME_CARD)).willReturn(SOME_CARD_ENTITY);

        DeckEntity deckEntity = deckEntityMapper.toEntity(SOME_DECK, SOME_GAME_ENTITY);

        assertThat(deckEntity).isInstanceOf(DeckEntity.class);
        assertThat(deckEntity.getId()).isEqualTo(SOME_DECK.getId());
        assertThat(deckEntity.getGame()).isEqualTo(SOME_GAME_ENTITY);
        assertThat(deckEntity.getCards()).containsExactly(SOME_CARD_ENTITY);
    }

    @Test
    void givenDeckEntityAttachedToGame__whenMappingFromEntity__thenReturnDeckAttachedToGame() {
        given(cardEntityMapper.fromEntity(SOME_CARD_ENTITY)).willReturn(SOME_CARD);

        Deck deck = deckEntityMapper.fromEntity(SOME_DECK_ENTITY_ATTACHED_TO_GAME);

        assertThat(deck).isInstanceOf(Deck.class);
        assertThat(deck.getId()).isEqualTo(SOME_DECK_ENTITY_ATTACHED_TO_GAME.getId());
        assertThat(deck.getGameId()).isEqualTo(SOME_GAME_ENTITY.getId());
        assertThat(deck.getCards()).containsExactly(SOME_CARD);
    }

    @Test
    void givenDeckNotAttachedToGame__whenMappingToEntity__thenReturnDeckEntityNotAttachedToGame() {
        given(cardEntityMapper.toEntity(SOME_CARD)).willReturn(SOME_CARD_ENTITY);

        DeckEntity deckEntity = deckEntityMapper.toEntity(SOME_DECK);

        assertThat(deckEntity).isInstanceOf(DeckEntity.class);
        assertThat(deckEntity.getId()).isEqualTo(SOME_DECK.getId());
        assertThat(deckEntity.getGame()).isNull();
        assertThat(deckEntity.getCards()).containsExactly(SOME_CARD_ENTITY);
    }

    @Test
    void
            givenDeckEntityNotAttachedToGame__whenMappingFromEntity__thenReturnDeckNotAttachedToGame() {
        given(cardEntityMapper.fromEntity(SOME_CARD_ENTITY)).willReturn(SOME_CARD);

        Deck deck = deckEntityMapper.fromEntity(SOME_DECK_ENTITY);

        assertThat(deck).isInstanceOf(Deck.class);
        assertThat(deck.getId()).isEqualTo(SOME_DECK_ENTITY.getId());
        assertThat(deck.getGameId()).isNull();
        assertThat(deck.getCards()).containsExactly(SOME_CARD);
    }
}
