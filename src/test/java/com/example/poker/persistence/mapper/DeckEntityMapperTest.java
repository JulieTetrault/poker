package com.example.poker.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.poker.domain.model.Deck;
import com.example.poker.fixture.DeckEntityFixture;
import com.example.poker.fixture.DeckFixture;
import com.example.poker.fixture.GameEntityFixture;
import com.example.poker.persistence.entity.DeckEntity;
import com.example.poker.persistence.entity.GameEntity;
import org.junit.jupiter.api.Test;

class DeckEntityMapperTest {
    private static final GameEntity SOME_GAME_ENTITY = new GameEntityFixture().build();
    private static final Deck SOME_DECK = new DeckFixture().build();
    private static final DeckEntity SOME_DECK_ENTITY =
            new DeckEntityFixture().withGame(null).build();
    private static final DeckEntity SOME_DECK_ENTITY_ATTACHED_TO_GAME =
            new DeckEntityFixture().withGame(SOME_GAME_ENTITY).build();

    private final DeckEntityMapper deckEntityMapper = new DeckEntityMapper();

    @Test
    void givenDeckAttachedToGame__whenMappingToEntity__thenReturnDeckEntityAttachedToGame() {
        DeckEntity deckEntity = deckEntityMapper.toEntity(SOME_DECK, SOME_GAME_ENTITY);

        assertThat(deckEntity).isInstanceOf(DeckEntity.class);
        assertThat(deckEntity.getId()).isEqualTo(SOME_DECK.getId());
        assertThat(deckEntity.getGame()).isEqualTo(SOME_GAME_ENTITY);
    }

    @Test
    void givenDeckEntityAttachedToGame__whenMappingFromEntity__thenReturnDeckAttachedToGame() {
        Deck deck = deckEntityMapper.fromEntity(SOME_DECK_ENTITY_ATTACHED_TO_GAME);

        assertThat(deck).isInstanceOf(Deck.class);
        assertThat(deck.getId()).isEqualTo(SOME_DECK_ENTITY_ATTACHED_TO_GAME.getId());
        assertThat(deck.getGameId()).isEqualTo(SOME_GAME_ENTITY.getId());
    }

    @Test
    void givenDeckNotAttachedToGame__whenMappingToEntity__thenReturnDeckEntityNotAttachedToGame() {
        DeckEntity deckEntity = deckEntityMapper.toEntity(SOME_DECK);

        assertThat(deckEntity).isInstanceOf(DeckEntity.class);
        assertThat(deckEntity.getId()).isEqualTo(SOME_DECK.getId());
        assertThat(deckEntity.getGame()).isNull();
    }

    @Test
    void givenDeckEntityNotAttachedToGame__whenMappingFromEntity__thenReturnDeckNotAttachedToGame() {
        Deck deck = deckEntityMapper.fromEntity(SOME_DECK_ENTITY);

        assertThat(deck).isInstanceOf(Deck.class);
        assertThat(deck.getId()).isEqualTo(SOME_DECK_ENTITY.getId());
        assertThat(deck.getGameId()).isNull();
    }
}
