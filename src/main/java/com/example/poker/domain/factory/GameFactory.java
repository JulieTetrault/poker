package com.example.poker.domain.factory;

import com.example.poker.domain.model.Game;
import org.springframework.stereotype.Component;

@Component
public final class GameFactory {
    private final IdGenerator idGenerator;

    public GameFactory(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public Game create(String name) {
        return new Game(idGenerator.nextId(), name);
    }
}
