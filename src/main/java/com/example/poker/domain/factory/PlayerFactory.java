package com.example.poker.domain.factory;

import com.example.poker.domain.model.Player;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public final class PlayerFactory {
    private final IdGenerator idGenerator;

    public PlayerFactory(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public Player create(UUID gameId, String name) {
        return new Player(idGenerator.nextId(), gameId, name);
    }
}
