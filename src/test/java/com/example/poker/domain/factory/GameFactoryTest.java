package com.example.poker.domain.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.example.poker.domain.model.Game;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GameFactoryTest {
    private static final String SOME_GAME_NAME = "Game";
    private static final UUID SOME_GAME_ID =
            UUID.fromString("715ba85d-f8a7-4f90-ae14-d974158f2fde");

    @Mock private IdGenerator idGenerator;

    @InjectMocks private GameFactory gameFactory;

    @BeforeEach
    void setUp() {
        given(idGenerator.nextId()).willReturn(SOME_GAME_ID);
    }

    @Nested
    @DisplayName("Creating a game")
    class Creation {
        @Test
        void whenCreating__thenGameHasGeneratedIdentityAndNoPlayers() {
            Game game = gameFactory.create(SOME_GAME_NAME);

            assertThat(game.getId()).isEqualTo(SOME_GAME_ID);
            assertThat(game.getName()).isEqualTo(SOME_GAME_NAME);
            assertThat(game.getShoe().getCards()).isEmpty();
            assertThat(game.getPlayers()).isEmpty();
            verify(idGenerator).nextId();
            verifyNoMoreInteractions(idGenerator);
        }
    }
}
