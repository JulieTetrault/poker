package com.example.poker.domain.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.example.poker.domain.model.Player;
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
class PlayerFactoryTest {
    private static final String SOME_PLAYER_NAME = "Alice";
    private static final UUID SOME_PLAYER_ID =
            UUID.fromString("715ba85d-f8a7-4f90-ae14-d974158f2fde");
    private static final UUID SOME_GAME_ID =
            UUID.fromString("f2c7be63-ac5c-4777-8a7c-ea97473818f1");

    @Mock private IdGenerator idGenerator;

    @InjectMocks private PlayerFactory playerFactory;

    @BeforeEach
    void setUp() {
        given(idGenerator.nextId()).willReturn(SOME_PLAYER_ID);
    }

    @Nested
    @DisplayName("Creating a player")
    class Creation {
        @Test
        void whenCreating__thenPlayerHasGeneratedIdentityAndEmptyHand() {
            Player player = playerFactory.create(SOME_GAME_ID, SOME_PLAYER_NAME);

            assertThat(player.getId()).isEqualTo(SOME_PLAYER_ID);
            assertThat(player.getGameId()).isEqualTo(SOME_GAME_ID);
            assertThat(player.getName()).isEqualTo(SOME_PLAYER_NAME);
            assertThat(player.getHand().getCards()).isEmpty();
            assertThat(player.getHandValue()).isZero();
        }
    }
}
