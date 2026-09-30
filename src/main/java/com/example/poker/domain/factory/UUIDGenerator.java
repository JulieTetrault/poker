package com.example.poker.domain.factory;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public final class UUIDGenerator implements IdGenerator {
    @Override
    public UUID nextId() {
        return UUID.randomUUID();
    }
}
