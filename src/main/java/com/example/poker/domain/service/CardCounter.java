package com.example.poker.domain.service;

import com.example.poker.domain.model.Card;
import com.example.poker.domain.model.Rank;
import com.example.poker.domain.model.Suit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class CardCounter {
    private final EnumMap<Suit, EnumMap<Rank, Integer>> cardCounts = new EnumMap<>(Suit.class);

    public void addCards(List<Card> cards) {
        cards.forEach(card -> adjustCardCount(card, 1));
    }

    public void removeCards(List<Card> cards) {
        cards.forEach(card -> adjustCardCount(card, -1));
    }

    public Map<Suit, Map<Rank, Integer>> getCardsCount() {
        Map<Suit, Map<Rank, Integer>> counts = new LinkedHashMap<>();
        for (Suit suit : Suit.values()) {
            counts.put(suit, getRankCounts(suit));
        }
        return Collections.unmodifiableMap(counts);
    }

    private Map<Rank, Integer> getRankCounts(Suit suit) {
        Map<Rank, Integer> counts = new LinkedHashMap<>();
        Arrays.stream(Rank.values())
                .sorted(Comparator.comparingInt(Rank::getValue).reversed())
                .forEach(rank -> counts.put(rank, getCardsCountBySuitAndRank(suit, rank)));
        return Collections.unmodifiableMap(counts);
    }

    public Map<Suit, Integer> getSuitCardsCount() {
        return Arrays.stream(Suit.values())
                .collect(
                        Collectors.toUnmodifiableMap(
                                Function.identity(), this::getCardsCountBySuit));
    }

    private int getCardsCountBySuit(Suit suit) {
        EnumMap<Rank, Integer> ranks = cardCounts.get(suit);
        return ranks == null ? 0 : ranks.values().stream().mapToInt(Integer::intValue).sum();
    }

    private int getCardsCountBySuitAndRank(Suit suit, Rank rank) {
        EnumMap<Rank, Integer> ranks = cardCounts.get(suit);
        return ranks == null ? 0 : ranks.getOrDefault(rank, 0);
    }

    private void adjustCardCount(Card card, int change) {
        cardCounts
                .computeIfAbsent(card.suit(), suit -> new EnumMap<>(Rank.class))
                .merge(card.rank(), change, Integer::sum);
    }
}
