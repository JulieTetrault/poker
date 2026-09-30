package com.example.poker.domain.service;

import com.example.poker.domain.model.Card;
import java.util.ArrayList;
import java.util.List;

public final class CardDealer {
    public List<Card> dealCards(List<Card> cards, int cardCount) {
        int dealtCount = Math.min(cardCount, cards.size());
        List<Card> dealtCards = new ArrayList<>(dealtCount);
        for (int i = 0; i < dealtCount; i++) {
            dealtCards.add(cards.removeLast());
        }
        return List.copyOf(dealtCards);
    }
}
