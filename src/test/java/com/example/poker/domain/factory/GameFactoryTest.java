package com.example.poker.domain.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.example.poker.domain.model.Game;
import com.example.poker.domain.model.Shoe;
import com.example.poker.fixture.ShoeFixture;
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
    private static final Shoe SOME_SHOE = new ShoeFixture().withGameId(SOME_GAME_ID).build();

    @Mock private IdGenerator idGenerator;

    @Mock private ShoeFactory shoeFactory;

    @InjectMocks private GameFactory gameFactory;

    @BeforeEach
    void setUp() {
        given(idGenerator.nextId()).willReturn(SOME_GAME_ID);
        given(shoeFactory.create(SOME_GAME_ID)).willReturn(SOME_SHOE);
    }

    @Nested
    @DisplayName("Creating a game")
    class Creation {
        @Test
        void whenCreating__thenGameHasGeneratedIdentityAndNoPlayers() {
            Game game = gameFactory.create(SOME_GAME_NAME);

            assertThat(game.getId()).isEqualTo(SOME_GAME_ID);
            assertThat(game.getName()).isEqualTo(SOME_GAME_NAME);
            assertThat(game.getShoe()).isSameAs(SOME_SHOE);
            assertThat(game.getPlayers()).isEmpty();
        }
    }
}
