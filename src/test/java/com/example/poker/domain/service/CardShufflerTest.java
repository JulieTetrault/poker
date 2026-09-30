package com.example.poker.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.example.poker.domain.model.Card;
import com.example.poker.domain.model.Deck;
import com.example.poker.domain.model.Rank;
import com.example.poker.fixture.CardFixture;
import com.example.poker.fixture.DeckFixture;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.random.RandomGenerator;
import org.junit.jupiter.api.Test;

class CardShufflerTest {
    private final CardShuffler cardShuffler = new CardShuffler();

    @Test
    void givenCards__whenShuffling__thenPermuteAllCardsUsingInclusiveBounds() {
        // GIVEN
        Card first = new CardFixture().withRank(Rank.TWO).build();
        Card second = new CardFixture().withRank(Rank.THREE).build();
        Card third = new CardFixture().withRank(Rank.FOUR).build();
        Deck deck = new DeckFixture().withCards(List.of(first, second, third)).build();
        List<Card> cards = new ArrayList<>(deck.getCards());
        RandomGenerator random = mock(RandomGenerator.class);
        given(random.nextInt(3)).willReturn(0);
        given(random.nextInt(2)).willReturn(0);
        // WHEN
        cardShuffler.shuffle(cards, random);
        // THEN
        assertThat(cards).containsExactly(second, third, first);
        assertThat(deck.getCards()).containsExactly(first, second, third);
        verify(random).nextInt(3);
        verify(random).nextInt(2);
        verifyNoMoreInteractions(random);
    }

    @Test
    void givenThreeCards__whenUsingEveryRandomChoiceSequence__thenProduceEveryPermutationOnce() {
        // GIVEN
        Card first = new CardFixture().withRank(Rank.ACE).build();
        Card second = new CardFixture().withRank(Rank.TWO).build();
        Card third = new CardFixture().withRank(Rank.THREE).build();
        Set<List<Card>> permutations = new HashSet<>();
        // WHEN
        for (int lastChoice = 0; lastChoice < 3; lastChoice++) {
            for (int middleChoice = 0; middleChoice < 2; middleChoice++) {
                List<Card> cards = new ArrayList<>(List.of(first, second, third));
                RandomGenerator random = mock(RandomGenerator.class);
                given(random.nextInt(3)).willReturn(lastChoice);
                given(random.nextInt(2)).willReturn(middleChoice);
                cardShuffler.shuffle(cards, random);
                permutations.add(List.copyOf(cards));
            }
        }
        // THEN
        assertThat(permutations).hasSize(6);
        for (List<Card> permutation : permutations) {
            assertThat(permutation).containsExactlyInAnyOrder(first, second, third);
        }
    }

    @Test
    void givenZeroOrOneCard__whenShuffling__thenDoNothingWithoutRandomChoices() {
        // GIVEN
        Card card = new CardFixture().build();
        RandomGenerator random = mock(RandomGenerator.class);
        List<Card> empty = new ArrayList<>();
        List<Card> single = new ArrayList<>(List.of(card));
        // WHEN
        cardShuffler.shuffle(empty, random);
        cardShuffler.shuffle(single, random);
        // THEN
        assertThat(empty).isEmpty();
        assertThat(single).containsExactly(card);
        verifyNoInteractions(random);
    }

    @Test
    void givenDuplicateCards__whenShufflingWithDefaultGenerator__thenPreserveOccurrences() {
        // GIVEN
        Card first = new CardFixture().withRank(Rank.ACE).build();
        Card second = new CardFixture().withRank(Rank.TWO).build();
        List<Card> cards = new ArrayList<>(List.of(first, second, first));
        // WHEN
        cardShuffler.shuffle(cards);
        // THEN
        assertThat(cards).containsExactlyInAnyOrder(first, second, first);
    }
}
