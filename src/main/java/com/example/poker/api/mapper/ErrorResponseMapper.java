package com.example.poker.api.mapper;

import com.example.poker.api.exception.InvalidIdentifierException;
import com.example.poker.api.response.ErrorResponse;
import com.example.poker.domain.exception.DeckAlreadyAttachedException;
import com.example.poker.domain.exception.NotFoundException;
import com.example.poker.domain.exception.PlayerNotFoundInGameException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ErrorResponseMapper {
    public ErrorResponse toResponse(NotFoundException exception) {
        return new ErrorResponse(
                exception.getMessage(), HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.name());
    }

    public ErrorResponse toResponse(PlayerNotFoundInGameException exception) {
        return new ErrorResponse(
                exception.getMessage(), HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.name());
    }

    public ErrorResponse toResponse(DeckAlreadyAttachedException exception) {
        return new ErrorResponse(
                exception.getMessage(),
                HttpStatus.UNPROCESSABLE_CONTENT.value(),
                HttpStatus.UNPROCESSABLE_CONTENT.name());
    }

    public ErrorResponse toResponse(InvalidIdentifierException exception) {
        return new ErrorResponse(
                exception.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.name());
    }

    public ErrorResponse toResponse(Exception exception) {
        return new ErrorResponse(
                "An unexpected error occurred.",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.name());
    }
}
