package com.example.poker.persistence.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.poker.fixture.DeckEntityFixture;
import com.example.poker.fixture.GameEntityFixture;
import org.junit.jupiter.api.Test;

class DeckEntityTest extends BaseEntityTest {
    private static final GameEntity SOME_GAME_ENTITY = new GameEntityFixture().build();
    private static final DeckEntity SOME_DECK_ENTITY =
            new DeckEntityFixture().withGame(SOME_GAME_ENTITY).build();

    @Test
    void givenDeckEntity__whenPersistingAndRetrieving__thenReturnDeckEntity() {
        persistEntity(SOME_GAME_ENTITY);
        persistEntity(SOME_DECK_ENTITY);

        withEntityManager(entityManager -> {
            DeckEntity deckEntity = entityManager.find(DeckEntity.class, SOME_DECK_ENTITY.getId());

            assertThat(deckEntity).isInstanceOf(DeckEntity.class);
            assertThat(deckEntity.getId()).isEqualTo(SOME_DECK_ENTITY.getId());
            assertThat(deckEntity.getGame().getId()).isEqualTo(SOME_GAME_ENTITY.getId());
        });
    }
}
