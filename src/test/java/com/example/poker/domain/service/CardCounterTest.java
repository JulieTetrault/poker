package com.example.poker.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.poker.domain.model.Card;
import com.example.poker.domain.model.Rank;
import com.example.poker.domain.model.Suit;
import com.example.poker.fixture.CardFixture;
import com.example.poker.fixture.DeckFixture;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CardCounterTest {
    private final CardCounter cardCounter = new CardCounter();

    @Test
    void givenRepeatedAndDealtCards__whenCountingAllCards__thenReturnOrderedImmutableSnapshot() {
        // GIVEN
        Card king = new CardFixture().withSuit(Suit.HEARTS).withRank(Rank.KING).build();
        Card ace = new CardFixture().withSuit(Suit.DIAMONDS).withRank(Rank.ACE).build();
        cardCounter.addCards(List.of(king, king, ace));
        cardCounter.removeCards(List.of(king));
        // WHEN
        Map<Suit, Map<Rank, Integer>> counts = cardCounter.getCardsCount();
        // THEN
        assertThat(counts.keySet())
                .containsExactly(Suit.HEARTS, Suit.SPADES, Suit.CLUBS, Suit.DIAMONDS);
        for (Map<Rank, Integer> suitCounts : counts.values()) {
            assertThat(suitCounts.keySet())
                    .containsExactly(
                            Rank.KING,
                            Rank.QUEEN,
                            Rank.JACK,
                            Rank.TEN,
                            Rank.NINE,
                            Rank.EIGHT,
                            Rank.SEVEN,
                            Rank.SIX,
                            Rank.FIVE,
                            Rank.FOUR,
                            Rank.THREE,
                            Rank.TWO,
                            Rank.ACE);
        }
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                int expected =
                        (suit == Suit.HEARTS && rank == Rank.KING)
                                        || (suit == Suit.DIAMONDS && rank == Rank.ACE)
                                ? 1
                                : 0;
                assertThat(counts.get(suit).get(rank)).isEqualTo(expected);
            }
        }
        assertThatThrownBy(() -> counts.clear()).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> counts.get(Suit.HEARTS).put(Rank.KING, 99))
                .isInstanceOf(UnsupportedOperationException.class);
        cardCounter.removeCards(List.of(king, ace));
        assertThat(counts.get(Suit.HEARTS).get(Rank.KING)).isEqualTo(1);
        assertThat(cardCounter.getCardsCount().get(Suit.HEARTS).get(Rank.KING)).isZero();
    }

    @Test
    void givenEmptyCounter__whenCounting__thenAllFacesAndSuitsAreZero() {
        // GIVEN
        // WHEN
        Map<Suit, Integer> counts = cardCounter.getSuitCardsCount();
        // THEN
        assertThat(counts)
                .isEqualTo(Map.of(Suit.HEARTS, 0, Suit.SPADES, 0, Suit.CLUBS, 0, Suit.DIAMONDS, 0));
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                assertThat(cardCounter.getCardsCount().get(suit).get(rank)).isZero();
            }
        }
    }

    @Test
    void givenRepeatedFaces__whenAddingCards__thenCountEveryOccurrence() {
        // GIVEN
        Card heart = new CardFixture().withSuit(Suit.HEARTS).withRank(Rank.ACE).build();
        Card spade = new CardFixture().withSuit(Suit.SPADES).withRank(Rank.KING).build();
        // WHEN
        cardCounter.addCards(List.of(heart, spade, heart));
        // THEN
        assertThat(cardCounter.getCardsCount().get(Suit.HEARTS).get(Rank.ACE)).isEqualTo(2);
        assertThat(cardCounter.getCardsCount().get(Suit.SPADES).get(Rank.KING)).isEqualTo(1);
        assertThat(cardCounter.getCardsCount().get(Suit.HEARTS).get(Rank.TWO)).isZero();
        assertThat(cardCounter.getSuitCardsCount())
                .isEqualTo(Map.of(Suit.HEARTS, 2, Suit.SPADES, 1, Suit.CLUBS, 0, Suit.DIAMONDS, 0));
    }

    @Test
    void givenTwoDecks__whenRemovingOne__thenRetainOtherDeckCounts() {
        // GIVEN
        List<Card> first = new DeckFixture().build().generateCards();
        List<Card> second = new DeckFixture().build().generateCards();
        cardCounter.addCards(first);
        cardCounter.addCards(second);
        // WHEN
        cardCounter.removeCards(first);
        // THEN
        assertThat(cardCounter.getSuitCardsCount())
                .isEqualTo(
                        Map.of(
                                Suit.HEARTS,
                                13,
                                Suit.SPADES,
                                13,
                                Suit.CLUBS,
                                13,
                                Suit.DIAMONDS,
                                13));
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                assertThat(cardCounter.getCardsCount().get(suit).get(rank)).isEqualTo(1);
            }
        }
    }

    @Test
    void givenConsumedCards__whenAddingMore__thenRestoreCountsForThoseFaces() {
        // GIVEN
        Card card = new CardFixture().withSuit(Suit.CLUBS).withRank(Rank.QUEEN).build();
        cardCounter.addCards(List.of(card));
        cardCounter.removeCards(List.of(card));
        // WHEN
        cardCounter.addCards(List.of(card, card));
        // THEN
        assertThat(cardCounter.getCardsCount().get(Suit.CLUBS).get(Rank.QUEEN)).isEqualTo(2);
        assertThat(cardCounter.getSuitCardsCount())
                .isEqualTo(Map.of(Suit.HEARTS, 0, Suit.SPADES, 0, Suit.CLUBS, 2, Suit.DIAMONDS, 0));
    }

    @Test
    void givenExistingCounts__whenAddingAndRemovingEmptyLists__thenPreserveCounts() {
        // GIVEN
        cardCounter.addCards(new DeckFixture().build().generateCards());
        // WHEN
        cardCounter.addCards(List.of());
        cardCounter.removeCards(List.of());
        // THEN
        assertThat(cardCounter.getSuitCardsCount())
                .isEqualTo(
                        Map.of(
                                Suit.HEARTS,
                                13,
                                Suit.SPADES,
                                13,
                                Suit.CLUBS,
                                13,
                                Suit.DIAMONDS,
                                13));
    }
}
