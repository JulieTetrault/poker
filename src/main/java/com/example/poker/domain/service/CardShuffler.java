package com.example.poker.domain.service;

import com.example.poker.domain.model.Card;
import java.util.List;
import java.util.random.RandomGenerator;

public final class CardShuffler {
    public void shuffle(List<Card> cards) {
        shuffle(cards, RandomGenerator.getDefault());
    }

    public void shuffle(List<Card> cards, RandomGenerator random) {
        for (int i = cards.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Card card = cards.get(i);
            cards.set(i, cards.get(j));
            cards.set(j, card);
        }
    }
}
