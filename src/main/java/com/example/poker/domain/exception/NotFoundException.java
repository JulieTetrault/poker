package com.example.poker.domain.exception;

import java.util.UUID;

public final class NotFoundException extends RuntimeException {
    private final String aggregateType;
    private final UUID id;

    public NotFoundException(String aggregateType, UUID id) {
        super(aggregateType + " not found: " + id);
        this.aggregateType = aggregateType;
        this.id = id;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public UUID getId() {
        return id;
    }
}
