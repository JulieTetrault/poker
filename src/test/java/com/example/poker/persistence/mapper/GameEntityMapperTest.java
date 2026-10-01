package com.example.poker.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.example.poker.domain.model.Deck;
import com.example.poker.domain.model.Game;
import com.example.poker.domain.model.Player;
import com.example.poker.fixture.DeckFixture;
import com.example.poker.fixture.GameEntityFixture;
import com.example.poker.fixture.GameFixture;
import com.example.poker.fixture.PlayerEntityFixture;
import com.example.poker.fixture.PlayerFixture;
import com.example.poker.persistence.entity.GameEntity;
import com.example.poker.persistence.entity.PlayerEntity;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GameEntityMapperTest {
    private static final Deck SOME_DECK = new DeckFixture().build();
    private static final Player SOME_PLAYER = new PlayerFixture().build();
    private static final PlayerEntity SOME_PLAYER_ENTITY = new PlayerEntityFixture().build();
    private static final Game SOME_GAME = new GameFixture()
            .withDecks(List.of(SOME_DECK))
            .withPlayers(List.of(SOME_PLAYER))
            .build();
    private static final GameEntity SOME_GAME_ENTITY =
            new GameEntityFixture().withPlayers(List.of(SOME_PLAYER_ENTITY)).build();

    @Spy
    private CardEntityMapper cardEntityMapper = new CardEntityMapper();

    @Mock
    private PlayerEntityMapper playerEntityMapper;

    @InjectMocks
    private GameEntityMapper gameEntityMapper;

    @Test
    void givenGame__whenMappingToEntity__thenReturnGameEntity() {
        given(playerEntityMapper.toEntity(eq(SOME_PLAYER), any(GameEntity.class)))
                .willReturn(SOME_PLAYER_ENTITY);

        GameEntity gameEntity = gameEntityMapper.toEntity(SOME_GAME);

        assertThat(gameEntity).isInstanceOf(GameEntity.class);
        assertThat(gameEntity.getId()).isEqualTo(SOME_GAME.getId());
        assertThat(gameEntity.getName()).isEqualTo(SOME_GAME.getName());
        assertThat(gameEntity.getPlayers()).containsExactly(SOME_PLAYER_ENTITY);
        assertThat(gameEntity.getUndealtCards())
                .containsExactlyElementsOf(SOME_GAME.getShoe().getCards().stream()
                        .map(cardEntityMapper::toEntity)
                        .toList());
    }

    @Test
    void givenGameEntity__whenMappingFromEntity__thenReturnGame() {
        given(playerEntityMapper.fromEntity(SOME_PLAYER_ENTITY)).willReturn(SOME_PLAYER);

        Game game = gameEntityMapper.fromEntity(SOME_GAME_ENTITY);

        assertThat(game).isInstanceOf(Game.class);
        assertThat(game.getId()).isEqualTo(SOME_GAME_ENTITY.getId());
        assertThat(game.getName()).isEqualTo(SOME_GAME_ENTITY.getName());
        assertThat(game.getPlayers()).containsExactly(SOME_PLAYER);
    }
}
