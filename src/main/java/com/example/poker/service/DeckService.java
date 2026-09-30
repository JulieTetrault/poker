package com.example.poker.service;

import com.example.poker.domain.factory.DeckFactory;
import com.example.poker.domain.model.Deck;
import com.example.poker.domain.repository.DeckRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DeckService {
    private final DeckFactory deckFactory;
    private final DeckRepository deckRepository;

    public DeckService(DeckFactory deckFactory, DeckRepository deckRepository) {
        this.deckFactory = deckFactory;
        this.deckRepository = deckRepository;
    }

    public Deck createDeck() {
        return deckRepository.create(deckFactory.create());
    }

    public Deck attachDeckToGame(UUID gameId, UUID deckId) {
        Deck deck = deckRepository.getById(deckId);
        deck.setGameId(gameId);
        return deckRepository.update(deck);
    }
}
