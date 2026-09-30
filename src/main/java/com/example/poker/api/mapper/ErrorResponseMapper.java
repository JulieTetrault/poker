package com.example.poker.api.mapper;

import com.example.poker.api.response.ErrorResponse;
import com.example.poker.domain.exception.DeckAlreadyAttachedException;
import com.example.poker.domain.exception.NotFoundException;
import com.example.poker.domain.exception.PlayerNotPartOfGameException;
import org.springframework.stereotype.Component;

@Component
public class ErrorResponseMapper {
    public ErrorResponse toResponse(NotFoundException exception) {
        String resource = exception.getAggregateType();
        String code =
                switch (resource) {
                    case "Game" -> "GAME_NOT_FOUND";
                    case "Player" -> "PLAYER_NOT_FOUND";
                    case "Deck" -> "DECK_NOT_FOUND";
                    default -> "RESOURCE_NOT_FOUND";
                };
        String label =
                switch (resource) {
                    case "Game", "Player", "Deck" -> resource;
                    default -> "Resource";
                };
        return new ErrorResponse(label + " '" + exception.getId() + "' was not found.", 404, code);
    }

    public ErrorResponse toResponse(PlayerNotPartOfGameException exception) {
        return new ErrorResponse(
                "Player '"
                        + exception.getPlayerId()
                        + "' was not found in game '"
                        + exception.getGameId()
                        + "'.",
                404,
                "PLAYER_NOT_FOUND");
    }

    public ErrorResponse toResponse(DeckAlreadyAttachedException exception) {
        return new ErrorResponse(
                "Deck '" + exception.getDeckId() + "' is already assigned to a game.",
                422,
                "DECK_ALREADY_ASSIGNED");
    }

    public ErrorResponse toResponse(Exception exception) {
        return new ErrorResponse("An unexpected error occurred.", 500, "INTERNAL_ERROR");
    }
}
