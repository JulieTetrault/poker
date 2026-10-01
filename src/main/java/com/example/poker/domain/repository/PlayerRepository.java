package com.example.poker.domain.repository;

import com.example.poker.domain.model.Player;
import java.util.UUID;

public interface PlayerRepository {
    Player getByIdAndGameId(UUID id, UUID gameId);
}
