package com.example.poker.persistence.repository;

import com.example.poker.domain.exception.NotFoundException;
import com.example.poker.domain.exception.PlayerNotFoundInGameException;
import com.example.poker.domain.model.Player;
import com.example.poker.domain.repository.PlayerRepository;
import com.example.poker.persistence.entity.PlayerEntity;
import com.example.poker.persistence.mapper.PlayerEntityMapper;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class InMemoryPlayerRepository implements PlayerRepository {
    private final JPAPlayerRepository playerRepository;
    private final PlayerEntityMapper playerEntityMapper;

    public InMemoryPlayerRepository(JPAPlayerRepository playerRepository, PlayerEntityMapper playerEntityMapper) {
        this.playerRepository = playerRepository;
        this.playerEntityMapper = playerEntityMapper;
    }

    @Override
    public Player getByIdAndGameId(UUID id, UUID gameId) {
        PlayerEntity player = this.getEntityById(id);
        if (!player.getGame().getId().equals(gameId)) {
            throw new PlayerNotFoundInGameException(id, gameId);
        }
        return playerEntityMapper.fromEntity(player);
    }

    private PlayerEntity getEntityById(UUID id) {
        return playerRepository.findById(id).orElseThrow(() -> new NotFoundException("Player", id));
    }
}
