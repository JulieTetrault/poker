package com.example.poker.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.poker.fixture.CardFixture;
import com.example.poker.fixture.PlayerFixture;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GameTest {
    private static final String SOME_GAME_NAME = "Game";
    private static final UUID SOME_GAME_ID =
            UUID.fromString("715ba85d-f8a7-4f90-ae14-d974158f2fde");
    private static final List<Card> HIGHEST_CARDS =
            List.of(new CardFixture().withRank(Rank.KING).build());
    private static final List<Card> LOWEST_CARDS =
            List.of(new CardFixture().withRank(Rank.TWO).build());
    private static final Player FIRST_PLAYER =
            new PlayerFixture()
                    .withGameId(SOME_GAME_ID)
                    .withName("Alice")
                    .withCards(HIGHEST_CARDS)
                    .build();
    private static final Player SECOND_PLAYER =
            new PlayerFixture()
                    .withGameId(SOME_GAME_ID)
                    .withName("John")
                    .withCards(LOWEST_CARDS)
                    .build();
    private static final Player THIRD_PLAYER =
            new PlayerFixture()
                    .withGameId(SOME_GAME_ID)
                    .withName("Bob")
                    .withCards(LOWEST_CARDS)
                    .build();

    @Test
    void givenEmptyGame__whenAddingPlayer__thenPlayerIsIncluded() {
        Game game = game();

        game.addPlayer(FIRST_PLAYER);

        assertThat(game.getPlayers()).containsExactly(FIRST_PLAYER);
    }

    @Test
    void givenGameWithPlayers__whenRemovingPlayer__thenOnlyThatPlayerIsRemoved() {
        Game game = game();
        game.addPlayer(FIRST_PLAYER);
        game.addPlayer(SECOND_PLAYER);

        game.removePlayer(FIRST_PLAYER.getId());

        assertThat(game.getPlayers()).containsExactly(SECOND_PLAYER);
    }

    @Test
    void
            givenPlayersWithDifferentAndEqualTotals__whenOrderingPlayers__thenOrderIsDescendingValueThenName() {
        Game game = game();
        game.addPlayer(FIRST_PLAYER);
        game.addPlayer(SECOND_PLAYER);
        game.addPlayer(THIRD_PLAYER);

        List<Player> players = game.getPlayersByHandValue();

        assertThat(players).containsExactly(FIRST_PLAYER, THIRD_PLAYER, SECOND_PLAYER);
    }

    private static Game game() {
        return new Game(SOME_GAME_ID, SOME_GAME_NAME);
    }
}
