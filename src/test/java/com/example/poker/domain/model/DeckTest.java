package com.example.poker.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DeckTest {
    private static final UUID SOME_DECK_ID =
            UUID.fromString("715ba85d-f8a7-4f90-ae14-d974158f2fde");
    private static final UUID SOME_GAME_ID =
            UUID.fromString("f2c7be63-ac5c-4777-8a7c-ea97473818f1");

    @Test
    void givenDeck__whenAssigningGameId__thenGameIdIsUpdated() {
        Deck deck = new Deck(SOME_DECK_ID);

        deck.setGameId(SOME_GAME_ID);

        assertThat(deck.getGameId()).isEqualTo(SOME_GAME_ID);
    }

    @Test
    void givenAttachedDeck__whenAssigningGameId__thenRejectAndPreserveOwnership() {
        // GIVEN
        Deck deck = new Deck(SOME_DECK_ID, SOME_GAME_ID);
        UUID otherGameId = UUID.fromString("2c513c68-0356-4202-8f9f-259d45d84576");
        // WHEN
        var exception = assertThatThrownBy(() -> deck.setGameId(otherGameId));
        // THEN
        exception
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Deck already attached to a game: " + SOME_DECK_ID);
        assertThat(deck.getGameId()).isEqualTo(SOME_GAME_ID);
    }

    @Test
    void givenAttachedDeck__whenAssigningSameGameId__thenReject() {
        // GIVEN
        Deck deck = new Deck(SOME_DECK_ID, SOME_GAME_ID);
        // WHEN
        var exception = assertThatThrownBy(() -> deck.setGameId(SOME_GAME_ID));
        // THEN
        exception
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Deck already attached to a game: " + SOME_DECK_ID);
        assertThat(deck.getGameId()).isEqualTo(SOME_GAME_ID);
    }

    @Test
    void whenGeneratingCards__thenReturnAllFiftyTwoFacesInSuitAndRankOrder() {
        // GIVEN
        Deck deck = new Deck(SOME_DECK_ID);
        // WHEN
        List<Card> cards = deck.generateCards();
        // THEN
        assertThat(cards).hasSize(52).doesNotHaveDuplicates().doesNotContainNull();
        for (Suit suit : Suit.values()) {
            assertThat(cards.stream().filter(card -> card.suit() == suit).map(Card::rank).toList())
                    .containsExactly(Rank.values());
        }
        cards.clear();
        assertThat(deck.generateCards()).hasSize(52);
    }
}
