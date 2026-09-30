package com.example.poker.persistence.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.poker.fixture.GameEntityFixture;
import com.example.poker.fixture.PlayerEntityFixture;
import org.junit.jupiter.api.Test;

class PlayerEntityTest extends BaseEntityTest {
    private static final GameEntity SOME_GAME_ENTITY = new GameEntityFixture().build();
    private static final PlayerEntity SOME_PLAYER_ENTITY =
            new PlayerEntityFixture().withGame(SOME_GAME_ENTITY).build();

    @Test
    void givenPlayerEntity__whenPersistingAndRetrieving__thenReturnPlayerEntity() {
        persistEntity(SOME_GAME_ENTITY);
        persistEntity(SOME_PLAYER_ENTITY);

        withEntityManager(
                entityManager -> {
                    PlayerEntity playerEntity =
                            entityManager.find(PlayerEntity.class, SOME_PLAYER_ENTITY.getId());

                    assertThat(playerEntity).isInstanceOf(PlayerEntity.class);
                    assertThat(playerEntity.getId()).isEqualTo(SOME_PLAYER_ENTITY.getId());
                    assertThat(playerEntity.getGame().getId()).isEqualTo(SOME_GAME_ENTITY.getId());
                    assertThat(playerEntity.getName()).isEqualTo(SOME_PLAYER_ENTITY.getName());
                    assertThat(playerEntity.getCards()).isEqualTo(SOME_PLAYER_ENTITY.getCards());
                });
    }
}
