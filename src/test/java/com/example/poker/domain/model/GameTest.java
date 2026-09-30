package com.example.poker.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.example.poker.fixture.CardFixture;
import com.example.poker.fixture.DeckFixture;
import com.example.poker.fixture.PlayerFixture;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GameTest {
    @Mock private Shoe shoe;
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

        game.removePlayer(FIRST_PLAYER);

        assertThat(game.getPlayers()).containsExactly(SECOND_PLAYER);
    }

    @Test
    void givenGameWithDeck__whenAddingDeck__thenBothDecksAreIncluded() {
        // GIVEN
        Game game = game();
        Deck first = new DeckFixture().withGameId(SOME_GAME_ID).build();
        Deck second = new DeckFixture().withGameId(SOME_GAME_ID).build();
        game.addDeck(first);
        // WHEN
        game.addDeck(second);
        // THEN
        verify(shoe).addDeck(first);
        verify(shoe).addDeck(second);
    }

    @Test
    void givenAbsentPlayer__whenRemovingPlayer__thenExistingPlayersRemain() {
        // GIVEN
        Game game = game();
        game.addPlayer(FIRST_PLAYER);
        // WHEN
        game.removePlayer(SECOND_PLAYER);
        // THEN
        assertThat(game.getPlayers()).containsExactly(FIRST_PLAYER);
    }

    private Game game() {
        return new Game(SOME_GAME_ID, SOME_GAME_NAME, shoe, Map.of());
    }

    @Test
    void givenPlayersAndCards__whenDealing__thenTransferCardsToOnlyTheSelectedPlayer() {
        // GIVEN
        Game game = game();
        Card first = new CardFixture().withRank(Rank.ACE).build();
        Card second = new CardFixture().withRank(Rank.TWO).build();
        Deck deck = new DeckFixture().build();
        Player selected =
                new PlayerFixture().withGameId(SOME_GAME_ID).withCards(List.of(second)).build();
        Player other = new PlayerFixture().withGameId(SOME_GAME_ID).build();
        game.addDeck(deck);
        game.addPlayer(selected);
        game.addPlayer(other);
        given(shoe.dealCards(2)).willReturn(List.of(first, second));
        // WHEN
        List<Card> dealt = game.dealCards(2, selected);
        // THEN
        assertThat(dealt).containsExactly(first, second);
        assertThat(selected.getCards()).containsExactly(second, first, second);
        assertThat(other.getCards()).isEmpty();
        assertThat(game.getPlayers()).containsExactlyInAnyOrder(selected, other);
        verify(shoe).dealCards(2);
    }

    @Test
    void givenFewerCardsThanRequested__whenDealing__thenAppendOnlyRemainingCards() {
        // GIVEN
        Game game = game();
        Card card = new CardFixture().build();
        Player player = new PlayerFixture().withGameId(SOME_GAME_ID).build();
        game.addPlayer(player);
        game.addDeck(new DeckFixture().build());
        given(shoe.dealCards(3)).willReturn(List.of(card, card));
        given(shoe.dealCards(1)).willReturn(List.of());
        // WHEN
        List<Card> dealt = game.dealCards(3, player);
        // THEN
        assertThat(dealt).containsExactly(card, card);
        assertThat(player.getCards()).containsExactly(card, card);
        verify(shoe).dealCards(3);
        assertThat(game.dealCards(1, player)).isEmpty();
        assertThat(player.getCards()).containsExactly(card, card);
    }

    @Test
    void givenSuppliedPlayer__whenDealing__thenAppendCardsWithoutChangingMembership() {
        // GIVEN
        Game game = game();
        Card card = new CardFixture().build();
        game.addDeck(new DeckFixture().build());
        Player player = new PlayerFixture().withGameId(SOME_GAME_ID).build();
        given(shoe.dealCards(1)).willReturn(List.of(card));
        // WHEN
        List<Card> dealt = game.dealCards(1, player);
        // THEN
        assertThat(dealt).containsExactly(card);
        assertThat(player.getCards()).containsExactly(card);
        verify(shoe).dealCards(1);
        assertThat(game.getPlayers()).isEmpty();
    }
}
