package com.example.poker.domain.factory;

import com.example.poker.domain.model.Shoe;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public final class ShoeFactory {
    private final IdGenerator idGenerator;

    public ShoeFactory(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public Shoe create(UUID gameId) {
        return new Shoe(idGenerator.nextId(), gameId);
    }
}
