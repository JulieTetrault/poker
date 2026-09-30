package com.example.poker.domain.repository;

import com.example.poker.domain.model.Game;
import java.util.UUID;

public interface GameRepository {
    Game getById(UUID id);

    Game create(Game game);

    Game update(Game game);

    void deleteById(UUID id);
}
