package com.example.poker.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.poker.fixture.CardFixture;
import java.util.List;
import org.junit.jupiter.api.Test;

class HandTest {
    private static final Card FIRST_CARD = new CardFixture().withRank(Rank.TWO).build();
    private static final Card SECOND_CARD = new CardFixture().withRank(Rank.THREE).build();
    private static final Card THIRD_CARD = new CardFixture().withRank(Rank.FOUR).build();

    @Test
    void givenEmptyHand__whenGettingValue__thenValueIsZero() {
        Hand hand = new Hand();

        int value = hand.getValue();

        assertThat(value).isZero();
    }

    @Test
    void givenHandWithCards__whenAddingMoreCards__thenCardsAccumulateAndValueIsSummed() {
        Hand hand = new Hand();
        hand.addCards(List.of(FIRST_CARD, SECOND_CARD));

        hand.addCards(List.of(THIRD_CARD));

        assertThat(hand.getCards()).containsExactly(FIRST_CARD, SECOND_CARD, THIRD_CARD);
        assertThat(hand.getValue()).isEqualTo(9);
    }
}
