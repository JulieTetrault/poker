package com.example.poker.domain.model;

import com.example.poker.domain.service.CardCounter;
import com.example.poker.domain.service.CardDealer;
import com.example.poker.domain.service.CardShuffler;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class Shoe {
    private final List<Card> cards;
    private final CardShuffler cardShuffler;
    private final CardCounter cardCounter;
    private final CardDealer cardDealer;

    public Shoe() {
        this(List.of());
    }

    public Shoe(List<Card> cards) {
        this(cards, new CardShuffler(), new CardCounter(), new CardDealer());
    }

    public Shoe(
            List<Card> cards,
            CardShuffler cardShuffler,
            CardCounter cardCounter,
            CardDealer cardDealer) {
        this.cards = new ArrayList<>(cards);
        this.cardShuffler = cardShuffler;
        this.cardCounter = cardCounter;
        this.cardDealer = cardDealer;
        cardCounter.addCards(this.cards);
    }

    public List<Card> getCards() {
        return List.copyOf(cards);
    }

    public void addDeck(Deck deck) {
        cards.addAll(deck.getCards());
        cardCounter.addCards(deck.getCards());
    }

    public List<Card> dealCards(int cardCount) {
        List<Card> dealt = cardDealer.dealCards(cards, cardCount);
        cardCounter.removeCards(dealt);
        return dealt;
    }

    public Map<Suit, Map<Rank, Integer>> getUndealtCardCounts() {
        return cardCounter.getCardsCount();
    }

    public Map<Suit, Integer> getUndealtSuitCardsCount() {
        return cardCounter.getSuitCardsCount();
    }

    public void shuffle() {
        cardShuffler.shuffle(cards);
    }
}
