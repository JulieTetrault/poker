package com.example.poker.service;

import com.example.poker.domain.factory.PlayerFactory;
import com.example.poker.domain.model.Player;
import com.example.poker.domain.repository.PlayerRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PlayerService {
    private final PlayerFactory playerFactory;
    private final PlayerRepository playerRepository;

    public PlayerService(PlayerFactory playerFactory, PlayerRepository playerRepository) {
        this.playerFactory = playerFactory;
        this.playerRepository = playerRepository;
    }

    public Player createPlayer(UUID gameId, String name) {
        return playerFactory.create(gameId, name);
    }

    public Player getPlayer(UUID gameId, UUID playerId) {
        return playerRepository.getByIdAndGameId(playerId, gameId);
    }
}
