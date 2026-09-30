package com.example.poker.domain.repository;

import com.example.poker.domain.model.Player;
import java.util.UUID;

public interface PlayerRepository {
    Player create(Player player);

    Player update(Player player);

    void deleteById(UUID id);
}
