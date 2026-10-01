package com.example.poker.api.exception;

public final class InvalidIdentifierException extends RuntimeException {
    public InvalidIdentifierException(String field) {
        super("Field '" + field + "' must be a valid UUID.");
    }
}
