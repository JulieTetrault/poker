package com.example.poker.domain.factory;

import com.example.poker.domain.model.Deck;
import org.springframework.stereotype.Component;

@Component
public final class DeckFactory {
    private final IdGenerator idGenerator;

    public DeckFactory(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public Deck create() {
        return new Deck(idGenerator.nextId());
    }
}
