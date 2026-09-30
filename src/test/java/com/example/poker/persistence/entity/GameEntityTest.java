package com.example.poker.persistence.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.poker.fixture.DeckEntityFixture;
import com.example.poker.fixture.GameEntityFixture;
import com.example.poker.fixture.PlayerEntityFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GameEntityTest extends BaseEntityTest {
    private GameEntity gameEntity;
    private DeckEntity deckEntity;
    private PlayerEntity playerEntity;

    @BeforeEach
    void setUp() {
        this.gameEntity = new GameEntityFixture().build();
        this.deckEntity = new DeckEntityFixture().withGame(this.gameEntity).build();
        this.playerEntity = new PlayerEntityFixture().withGame(this.gameEntity).build();
        this.gameEntity.addDeck(this.deckEntity);
        this.gameEntity.addPlayer(this.playerEntity);
    }

    @Test
    void givenGameEntity__whenPersistingAndRetrieving__thenReturnGameEntity() {
        persistEntity(this.gameEntity);

        withEntityManager(
                entityManager -> {
                    GameEntity gameEntity =
                            entityManager.find(GameEntity.class, this.gameEntity.getId());

                    assertThat(gameEntity).isInstanceOf(GameEntity.class);
                    assertThat(gameEntity.getId()).isEqualTo(this.gameEntity.getId());
                    assertThat(gameEntity.getName()).isEqualTo(this.gameEntity.getName());
                    assertThat(gameEntity.getDecks())
                            .extracting(DeckEntity::getId)
                            .containsExactly(this.deckEntity.getId());
                    assertThat(gameEntity.getPlayers())
                            .extracting(PlayerEntity::getId)
                            .containsExactly(this.playerEntity.getId());
                });
    }
}
