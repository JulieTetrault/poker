package com.example.poker.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.poker.domain.model.Card;
import com.example.poker.domain.model.Rank;
import com.example.poker.fixture.CardFixture;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class CardDealerTest {
    private final CardDealer dealer = new CardDealer();

    @Test
    void givenCards__whenDealing__thenRemoveFromEndAndReturnImmutableHand() {
        // GIVEN
        Card first = new CardFixture().withRank(Rank.ACE).build();
        Card second = new CardFixture().withRank(Rank.TWO).build();
        Card third = new CardFixture().withRank(Rank.THREE).build();
        List<Card> cards = new ArrayList<>(List.of(first, second, third));
        // WHEN
        List<Card> dealt = dealer.dealCards(cards, 2);
        // THEN
        assertThat(dealt).containsExactly(third, second);
        assertThat(cards).containsExactly(first);
        assertThatThrownBy(() -> dealt.add(first))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThat(dealer.dealCards(cards, 1)).containsExactly(first);
        assertThat(dealt).containsExactly(third, second);
    }

    @Test
    void givenRepeatedCards__whenRequestExceedsRemainingCards__thenDealEveryOccurrence() {
        // GIVEN
        Card card = new CardFixture().build();
        List<Card> cards = new ArrayList<>(List.of(card, card));
        // WHEN
        List<Card> dealt = dealer.dealCards(cards, 3);
        // THEN
        assertThat(dealt).containsExactly(card, card);
        assertThat(cards).isEmpty();
        assertThat(dealer.dealCards(cards, 1)).isEmpty();
    }

    @Test
    void givenCards__whenDealingZero__thenLeaveCardsUnchanged() {
        // GIVEN
        Card card = new CardFixture().build();
        List<Card> cards = new ArrayList<>(List.of(card));
        // WHEN
        List<Card> dealt = dealer.dealCards(cards, 0);
        // THEN
        assertThat(dealt).isEmpty();
        assertThat(cards).containsExactly(card);
    }
}
