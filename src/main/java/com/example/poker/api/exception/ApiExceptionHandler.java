package com.example.poker.api.exception;

import com.example.poker.api.mapper.ErrorResponseMapper;
import com.example.poker.api.response.ErrorResponse;
import com.example.poker.domain.exception.DeckAlreadyAttachedException;
import com.example.poker.domain.exception.NotFoundException;
import com.example.poker.domain.exception.PlayerNotPartOfGameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);
    private final ErrorResponseMapper errorResponseMapper;

    public ApiExceptionHandler(ErrorResponseMapper errorResponseMapper) {
        this.errorResponseMapper = errorResponseMapper;
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException exception) {
        return toResponseEntity(errorResponseMapper.toResponse(exception));
    }

    @ExceptionHandler(PlayerNotPartOfGameException.class)
    public ResponseEntity<ErrorResponse> handlePlayerNotPartOfGame(
            PlayerNotPartOfGameException exception) {
        return toResponseEntity(errorResponseMapper.toResponse(exception));
    }

    @ExceptionHandler(DeckAlreadyAttachedException.class)
    public ResponseEntity<ErrorResponse> handleDeckAlreadyAttached(
            DeckAlreadyAttachedException exception) {
        return toResponseEntity(errorResponseMapper.toResponse(exception));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception exception) {
        LOGGER.error("Unexpected error while handling an API request", exception);
        return toResponseEntity(errorResponseMapper.toResponse(exception));
    }

    private ResponseEntity<ErrorResponse> toResponseEntity(ErrorResponse response) {
        return ResponseEntity.status(response.status())
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(response);
    }
}
