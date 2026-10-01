package com.example.poker.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.example.poker.domain.exception.PlayerNotFoundInGameException;
import com.example.poker.domain.model.Player;
import com.example.poker.fixture.GameEntityFixture;
import com.example.poker.fixture.PlayerEntityFixture;
import com.example.poker.fixture.PlayerFixture;
import com.example.poker.persistence.entity.GameEntity;
import com.example.poker.persistence.entity.PlayerEntity;
import com.example.poker.persistence.mapper.PlayerEntityMapper;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InMemoryPlayerRepositoryTest {
    private static final GameEntity SOME_OTHER_GAME_ENTITY = new GameEntityFixture().build();
    private static final Player SOME_PLAYER = new PlayerFixture().build();
    private static final Player SOME_PERSISTED_PLAYER = new PlayerFixture().build();
    private static final PlayerEntity SOME_PLAYER_ENTITY = new PlayerEntityFixture()
            .withId(SOME_PLAYER.getId())
            .withGame(new GameEntityFixture().withId(SOME_PLAYER.getGameId()).build())
            .build();

    @Mock
    private JPAPlayerRepository playerRepository;

    @Mock
    private PlayerEntityMapper playerEntityMapper;

    @InjectMocks
    private InMemoryPlayerRepository inMemoryPlayerRepository;

    @Test
    void givenExistingPlayerInGame__whenGetting__thenReturnEntity() {
        given(playerRepository.findById(SOME_PLAYER.getId())).willReturn(Optional.of(SOME_PLAYER_ENTITY));
        given(playerEntityMapper.fromEntity(SOME_PLAYER_ENTITY)).willReturn(SOME_PERSISTED_PLAYER);

        Player player = inMemoryPlayerRepository.getByIdAndGameId(SOME_PLAYER.getId(), SOME_PLAYER.getGameId());

        assertThat(player).isSameAs(SOME_PERSISTED_PLAYER);
    }

    @Test
    void givenMissingPlayerInExistingGame__whenGetting__thenThrowNotFoundException() {
        given(playerRepository.findById(SOME_PLAYER.getId())).willReturn(Optional.of(SOME_PLAYER_ENTITY));

        var exception = assertThatThrownBy(
                () -> inMemoryPlayerRepository.getByIdAndGameId(SOME_PLAYER.getId(), SOME_OTHER_GAME_ENTITY.getId()));

        exception
                .isInstanceOf(PlayerNotFoundInGameException.class)
                .hasMessage(
                        "Player " + SOME_PLAYER.getId() + " is not found in game " + SOME_OTHER_GAME_ENTITY.getId());
    }
}
