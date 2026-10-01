package com.example.poker.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.example.poker.domain.exception.NotFoundException;
import com.example.poker.domain.exception.PlayerNotFoundInGameException;
import com.example.poker.domain.model.Player;
import com.example.poker.fixture.GameEntityFixture;
import com.example.poker.fixture.PlayerEntityFixture;
import com.example.poker.fixture.PlayerFixture;
import com.example.poker.persistence.entity.GameEntity;
import com.example.poker.persistence.entity.PlayerEntity;
import com.example.poker.persistence.mapper.PlayerEntityMapper;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InMemoryPlayerRepositoryTest {
    private static final GameEntity SOME_GAME_ENTITY = new GameEntityFixture().build();
    private static final Player SOME_PLAYER = new PlayerFixture().build();
    private static final Player SOME_PERSISTED_PLAYER = new PlayerFixture().build();
    private static final PlayerEntity SOME_PLAYER_ENTITY = new PlayerEntityFixture().build();
    private static final PlayerEntity SOME_PERSISTED_PLAYER_ENTITY =
            new PlayerEntityFixture().build();

    @Mock private JPAPlayerRepository playerRepository;

    @Mock private PlayerEntityMapper playerEntityMapper;

    @Mock private EntityManager entityManager;

    @InjectMocks private InMemoryPlayerRepository inMemoryPlayerRepository;

    @Test
    void whenCreating__thenReturnPersistedPlayer() {
        given(entityManager.getReference(GameEntity.class, SOME_PLAYER.getGameId()))
                .willReturn(SOME_GAME_ENTITY);
        given(playerEntityMapper.toEntity(SOME_PLAYER, SOME_GAME_ENTITY))
                .willReturn(SOME_PLAYER_ENTITY);
        given(playerRepository.save(SOME_PLAYER_ENTITY)).willReturn(SOME_PERSISTED_PLAYER_ENTITY);
        given(playerEntityMapper.fromEntity(SOME_PERSISTED_PLAYER_ENTITY))
                .willReturn(SOME_PERSISTED_PLAYER);

        Player player = inMemoryPlayerRepository.create(SOME_PLAYER);

        assertThat(player).isSameAs(SOME_PERSISTED_PLAYER);
        verify(playerRepository).save(SOME_PLAYER_ENTITY);
    }

    @Test
    void givenExistingPlayer__whenUpdating__thenReturnPersistedPlayer() {
        given(playerRepository.findById(SOME_PLAYER.getId()))
                .willReturn(Optional.of(SOME_PLAYER_ENTITY));
        given(playerEntityMapper.toEntity(SOME_PLAYER, SOME_PLAYER_ENTITY.getGame()))
                .willReturn(SOME_PLAYER_ENTITY);
        given(playerRepository.save(SOME_PLAYER_ENTITY)).willReturn(SOME_PERSISTED_PLAYER_ENTITY);
        given(playerEntityMapper.fromEntity(SOME_PERSISTED_PLAYER_ENTITY))
                .willReturn(SOME_PERSISTED_PLAYER);

        Player player = inMemoryPlayerRepository.update(SOME_PLAYER);

        assertThat(player).isSameAs(SOME_PERSISTED_PLAYER);
        verify(playerRepository).save(SOME_PLAYER_ENTITY);
        verifyNoInteractions(entityManager);
    }

    @Test
    void givenMissingPlayer__whenUpdating__thenThrowNotFoundException() {
        given(playerRepository.findById(SOME_PLAYER.getId())).willReturn(Optional.empty());

        var exception = assertThatThrownBy(() -> inMemoryPlayerRepository.update(SOME_PLAYER));

        exception
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Player not found: " + SOME_PLAYER.getId());
        verify(playerRepository).findById(SOME_PLAYER.getId());
        verifyNoMoreInteractions(playerRepository);
        verifyNoInteractions(playerEntityMapper, entityManager);
    }

    @Test
    void givenExistingPlayer__whenDeleting__thenDeleteEntity() {
        given(playerRepository.findById(SOME_PLAYER.getId()))
                .willReturn(Optional.of(SOME_PLAYER_ENTITY));

        inMemoryPlayerRepository.deleteById(SOME_PLAYER.getId());

        verify(playerRepository).delete(SOME_PLAYER_ENTITY);
        verify(playerRepository).findById(SOME_PLAYER.getId());
        verifyNoMoreInteractions(playerRepository);
        verifyNoInteractions(playerEntityMapper, entityManager);
    }

    @Test
    void givenMissingPlayer__whenDeleting__thenThrowNotFoundException() {
        given(playerRepository.findById(SOME_PLAYER.getId())).willReturn(Optional.empty());

        var exception =
                assertThatThrownBy(() -> inMemoryPlayerRepository.deleteById(SOME_PLAYER.getId()));

        exception
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Player not found: " + SOME_PLAYER.getId());
        verify(playerRepository).findById(SOME_PLAYER.getId());
        verifyNoMoreInteractions(playerRepository);
        verifyNoInteractions(playerEntityMapper, entityManager);
    }

    @Test
    void givenMatchingGame__whenGettingPlayer__thenReturnPlayer() {
        // GIVEN
        given(playerRepository.findById(SOME_PLAYER_ENTITY.getId()))
                .willReturn(Optional.of(SOME_PLAYER_ENTITY));
        given(playerEntityMapper.fromEntity(SOME_PLAYER_ENTITY)).willReturn(SOME_PLAYER);
        // WHEN
        Player player =
                inMemoryPlayerRepository.getByIdAndGameId(
                        SOME_PLAYER_ENTITY.getId(), SOME_PLAYER_ENTITY.getGame().getId());
        // THEN
        assertThat(player).isSameAs(SOME_PLAYER);
        verify(playerRepository).findById(SOME_PLAYER_ENTITY.getId());
        verifyNoMoreInteractions(playerRepository);
        verifyNoInteractions(entityManager);
    }

    @Test
    void givenOtherGame__whenGettingPlayer__thenThrowPlayerNotPartOfGameException() {
        // GIVEN
        given(playerRepository.findById(SOME_PLAYER_ENTITY.getId()))
                .willReturn(Optional.of(SOME_PLAYER_ENTITY));
        // WHEN
        var exception =
                assertThatThrownBy(
                        () ->
                                inMemoryPlayerRepository.getByIdAndGameId(
                                        SOME_PLAYER_ENTITY.getId(), SOME_GAME_ENTITY.getId()));
        // THEN
        exception
                .isInstanceOf(PlayerNotFoundInGameException.class)
                .hasMessage(
                        "Player "
                                + SOME_PLAYER_ENTITY.getId()
                                + " is not found in game "
                                + SOME_GAME_ENTITY.getId());
        verify(playerRepository).findById(SOME_PLAYER_ENTITY.getId());
        verifyNoMoreInteractions(playerRepository);
        verifyNoInteractions(playerEntityMapper, entityManager);
    }

    @Test
    void givenMissingPlayer__whenGettingPlayer__thenThrowNotFoundException() {
        // GIVEN
        given(playerRepository.findById(SOME_PLAYER.getId())).willReturn(Optional.empty());
        // WHEN
        var exception =
                assertThatThrownBy(
                        () ->
                                inMemoryPlayerRepository.getByIdAndGameId(
                                        SOME_PLAYER.getId(), SOME_GAME_ENTITY.getId()));
        // THEN
        exception
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Player not found: " + SOME_PLAYER.getId());
        verify(playerRepository).findById(SOME_PLAYER.getId());
        verifyNoMoreInteractions(playerRepository);
        verifyNoInteractions(playerEntityMapper, entityManager);
    }
}
