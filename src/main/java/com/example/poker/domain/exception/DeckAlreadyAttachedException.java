package com.example.poker.domain.exception;

import java.util.UUID;

public final class DeckAlreadyAttachedException extends IllegalStateException {
    private final UUID deckId;

    public DeckAlreadyAttachedException(UUID deckId) {
        super("Deck already attached: " + deckId);
        this.deckId = deckId;
    }

    public UUID getDeckId() {
        return deckId;
    }
}
