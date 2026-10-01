package com.example.poker.domain.exception;

import java.util.UUID;

public final class NotFoundException extends RuntimeException {
    public NotFoundException(String aggregateType, UUID id) {
        super(aggregateType + " not found: " + id);
    }
}
