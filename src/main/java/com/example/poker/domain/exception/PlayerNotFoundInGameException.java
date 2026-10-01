package com.example.poker.domain.exception;

import java.util.UUID;

public final class PlayerNotFoundInGameException extends RuntimeException {
    public PlayerNotFoundInGameException(UUID playerId, UUID gameId) {
        super("Player " + playerId + " is not found in game " + gameId);
    }
}
