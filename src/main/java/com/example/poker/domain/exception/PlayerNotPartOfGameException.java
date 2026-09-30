package com.example.poker.domain.exception;

import java.util.UUID;

public final class PlayerNotPartOfGameException extends RuntimeException {
    public PlayerNotPartOfGameException(UUID playerId, UUID gameId) {
        super("Player " + playerId + " is not part of game: " + gameId);
    }
}
