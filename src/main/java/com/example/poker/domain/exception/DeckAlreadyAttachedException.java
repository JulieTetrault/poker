package com.example.poker.domain.exception;

import java.util.UUID;

public final class DeckAlreadyAttachedException extends IllegalStateException {
    public DeckAlreadyAttachedException(UUID deckId) {
        super("Deck already attached to a game: " + deckId);
    }
}
