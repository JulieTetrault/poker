package com.example.poker.domain.exception;

import java.util.UUID;

public final class PlayerNotPartOfGameException extends RuntimeException {
    private final UUID playerId;
    private final UUID gameId;

    public PlayerNotPartOfGameException(UUID playerId, UUID gameId) {
        super("Player " + playerId + " is not part of game: " + gameId);
        this.playerId = playerId;
        this.gameId = gameId;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public UUID getGameId() {
        return gameId;
    }
}
