package com.example.poker.domain.repository;

import com.example.poker.domain.model.Deck;

public interface DeckRepository {
    Deck create(Deck deck);

    Deck update(Deck deck);
}
