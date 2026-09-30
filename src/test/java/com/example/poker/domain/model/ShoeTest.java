package com.example.poker.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.example.poker.domain.service.CardCounter;
import com.example.poker.domain.service.CardDealer;
import com.example.poker.domain.service.CardShuffler;
import com.example.poker.fixture.CardFixture;
import com.example.poker.fixture.DeckFixture;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShoeTest {
    @Mock private CardDealer cardDealer;
    @Mock private CardCounter cardCounter;
    @Mock private CardShuffler cardShuffler;

    @Test
    void givenRestoredCards__whenCreatingAndAddingDeck__thenInitializeAndUpdateCounter() {
        // GIVEN
        Card card = new CardFixture().build();
        Deck deck = new DeckFixture().build();
        // WHEN
        Shoe shoe = new Shoe(List.of(card), cardShuffler, cardCounter, cardDealer);
        verify(cardCounter).addCards(List.of(card));
        shoe.addDeck(deck);
        // THEN
        assertThat(shoe.getCards())
                .containsExactlyElementsOf(
                        java.util.stream.Stream.concat(
                                        List.of(card).stream(), deck.generateCards().stream())
                                .toList());
        verify(cardCounter).addCards(deck.generateCards());
        verifyNoInteractions(cardDealer, cardShuffler);
    }

    @Test
    void givenCards__whenDealing__thenDelegateToDealerAndUpdateCounter() {
        // GIVEN
        Card card = new CardFixture().build();
        Shoe shoe = new Shoe(List.of(card), cardShuffler, cardCounter, cardDealer);
        List<Card> dealt = List.of(card);
        given(cardDealer.dealCards(List.of(card), 1)).willReturn(dealt);
        // WHEN
        List<Card> result = shoe.dealCards(1);
        // THEN
        assertThat(result).isSameAs(dealt);
        verify(cardDealer).dealCards(List.of(card), 1);
        verify(cardCounter).removeCards(dealt);
        verifyNoInteractions(cardShuffler);
    }

    @Test
    void givenCards__whenShuffling__thenDelegateWithoutChangingCounts() {
        // GIVEN
        Card card = new CardFixture().build();
        Shoe shoe = new Shoe(List.of(card), cardShuffler, cardCounter, cardDealer);
        // WHEN
        shoe.shuffle();
        // THEN
        verify(cardShuffler).shuffle(List.of(card));
        verify(cardCounter).addCards(List.of(card));
        verifyNoMoreInteractions(cardCounter);
        verifyNoInteractions(cardDealer);
    }

    @Test
    void givenCounterSnapshots__whenCounting__thenReturnCounterResults() {
        // GIVEN
        Shoe shoe = new Shoe(List.of(), cardShuffler, cardCounter, cardDealer);
        Map<Suit, Integer> suits = Map.of(Suit.HEARTS, 2);
        Map<Suit, Map<Rank, Integer>> faces = Map.of(Suit.HEARTS, Map.of(Rank.KING, 2));
        given(cardCounter.getSuitCardsCount()).willReturn(suits);
        given(cardCounter.getCardsCount()).willReturn(faces);
        // WHEN
        Map<Suit, Integer> suitCounts = shoe.getUndealtSuitCardsCount();
        Map<Suit, Map<Rank, Integer>> faceCounts = shoe.getUndealtCardCounts();
        // THEN
        assertThat(suitCounts).isSameAs(suits);
        assertThat(faceCounts).isSameAs(faces);
        verifyNoInteractions(cardDealer, cardShuffler);
    }

    @Test
    void givenTwoDecks__whenAddingAndExhausting__thenDealAllGeneratedOccurrencesOnce() {
        // GIVEN
        Shoe shoe = new Shoe();
        Deck first = new DeckFixture().build();
        Deck second = new DeckFixture().build();
        // WHEN
        shoe.addDeck(first);
        shoe.addDeck(second);
        // THEN
        assertThat(shoe.getCards()).hasSize(104);
        assertThat(shoe.getUndealtSuitCardsCount().values()).containsOnly(26);
        List<Card> dealt = shoe.dealCards(104);
        assertThat(dealt).hasSize(104);
        assertThat(dealt.stream().distinct()).hasSize(52);
        assertThat(shoe.getCards()).isEmpty();
        assertThat(shoe.dealCards(1)).isEmpty();
        assertThat(shoe.getUndealtSuitCardsCount().values()).containsOnly(0);
    }
}
