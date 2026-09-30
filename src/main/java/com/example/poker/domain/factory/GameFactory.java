package com.example.poker.domain.factory;

import com.example.poker.domain.model.Game;
import com.example.poker.domain.model.Shoe;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public final class GameFactory {
    private final IdGenerator idGenerator;

    private final ShoeFactory shoeFactory;

    public GameFactory(IdGenerator idGenerator, ShoeFactory shoeFactory) {
        this.idGenerator = idGenerator;
        this.shoeFactory = shoeFactory;
    }

    public Game create(String name) {
        UUID id = idGenerator.nextId();
        Shoe shoe = shoeFactory.create(id);
        return new Game(id, name, shoe);
    }
}
