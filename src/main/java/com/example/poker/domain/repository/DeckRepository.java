package com.example.poker.domain.repository;

import com.example.poker.domain.model.Deck;
import java.util.UUID;

public interface DeckRepository {
    Deck getById(UUID id);

    Deck create(Deck deck);

    Deck update(Deck deck);
}
