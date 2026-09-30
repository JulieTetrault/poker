package com.example.poker.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.example.poker.domain.exception.NotFoundException;
import com.example.poker.domain.model.Game;
import com.example.poker.fixture.GameEntityFixture;
import com.example.poker.fixture.GameFixture;
import com.example.poker.persistence.entity.GameEntity;
import com.example.poker.persistence.mapper.GameEntityMapper;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InMemoryGameRepositoryTest {
    private static final Game SOME_GAME = new GameFixture().build();
    private static final Game SOME_PERSISTED_GAME = new GameFixture().build();
    private static final GameEntity SOME_GAME_ENTITY = new GameEntityFixture().build();
    private static final GameEntity SOME_PERSISTED_GAME_ENTITY = new GameEntityFixture().build();

    @Mock private JPAGameRepository gameRepository;

    @Mock private GameEntityMapper gameEntityMapper;

    @InjectMocks private InMemoryGameRepository inMemoryGameRepository;

    @Test
    void whenCreating__thenReturnPersistedGame() {
        given(gameEntityMapper.toEntity(SOME_GAME)).willReturn(SOME_GAME_ENTITY);
        given(gameRepository.save(SOME_GAME_ENTITY)).willReturn(SOME_PERSISTED_GAME_ENTITY);
        given(gameEntityMapper.fromEntity(SOME_PERSISTED_GAME_ENTITY))
                .willReturn(SOME_PERSISTED_GAME);

        Game game = inMemoryGameRepository.create(SOME_GAME);

        assertThat(game).isSameAs(SOME_PERSISTED_GAME);
        verify(gameRepository).save(SOME_GAME_ENTITY);
    }

    @Test
    void givenExistingGame__whenUpdating__thenReturnPersistedGame() {
        given(gameRepository.findById(SOME_GAME.getId())).willReturn(Optional.of(SOME_GAME_ENTITY));
        given(gameEntityMapper.toEntity(SOME_GAME)).willReturn(SOME_GAME_ENTITY);
        given(gameRepository.save(SOME_GAME_ENTITY)).willReturn(SOME_PERSISTED_GAME_ENTITY);
        given(gameEntityMapper.fromEntity(SOME_PERSISTED_GAME_ENTITY))
                .willReturn(SOME_PERSISTED_GAME);

        Game game = inMemoryGameRepository.update(SOME_GAME);

        assertThat(game).isSameAs(SOME_PERSISTED_GAME);
        verify(gameRepository).save(SOME_GAME_ENTITY);
    }

    @Test
    void givenMissingGame__whenUpdating__thenThrowNotFoundException() {
        given(gameRepository.findById(SOME_GAME.getId())).willReturn(Optional.empty());

        var exception = assertThatThrownBy(() -> inMemoryGameRepository.update(SOME_GAME));

        exception
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Game not found: " + SOME_GAME.getId());
        verify(gameRepository).findById(SOME_GAME.getId());
        verifyNoMoreInteractions(gameRepository);
        verifyNoInteractions(gameEntityMapper);
    }

    @Test
    void givenExistingGame__whenDeleting__thenDeleteGame() {
        given(gameRepository.findById(SOME_GAME.getId())).willReturn(Optional.of(SOME_GAME_ENTITY));

        inMemoryGameRepository.deleteById(SOME_GAME.getId());

        verify(gameRepository).delete(SOME_GAME_ENTITY);
        verify(gameRepository).findById(SOME_GAME.getId());
        verifyNoMoreInteractions(gameRepository);
        verifyNoInteractions(gameEntityMapper);
    }

    @Test
    void givenMissingGame__whenDeleting__thenThrowNotFoundException() {
        given(gameRepository.findById(SOME_GAME.getId())).willReturn(Optional.empty());

        var exception =
                assertThatThrownBy(() -> inMemoryGameRepository.deleteById(SOME_GAME.getId()));

        exception
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Game not found: " + SOME_GAME.getId());
        verify(gameRepository).findById(SOME_GAME.getId());
        verifyNoMoreInteractions(gameRepository);
        verifyNoInteractions(gameEntityMapper);
    }
}
