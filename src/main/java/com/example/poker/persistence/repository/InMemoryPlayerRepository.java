package com.example.poker.persistence.repository;

import com.example.poker.domain.exception.NotFoundException;
import com.example.poker.domain.exception.PlayerNotPartOfGameException;
import com.example.poker.domain.model.Player;
import com.example.poker.domain.repository.PlayerRepository;
import com.example.poker.persistence.entity.GameEntity;
import com.example.poker.persistence.entity.PlayerEntity;
import com.example.poker.persistence.mapper.PlayerEntityMapper;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class InMemoryPlayerRepository implements PlayerRepository {
    private final JPAPlayerRepository playerRepository;
    private final PlayerEntityMapper playerEntityMapper;
    private final EntityManager entityManager;

    public InMemoryPlayerRepository(
            JPAPlayerRepository playerRepository,
            PlayerEntityMapper playerEntityMapper,
            EntityManager entityManager) {
        this.playerRepository = playerRepository;
        this.playerEntityMapper = playerEntityMapper;
        this.entityManager = entityManager;
    }

    @Override
    public Player create(Player player) {
        GameEntity gameEntity = this.resolveGameEntity(player);
        return playerEntityMapper.fromEntity(
                playerRepository.save(playerEntityMapper.toEntity(player, gameEntity)));
    }

    @Override
    public Player update(Player player) {
        PlayerEntity playerEntity = this.getEntityById(player.getId());
        return playerEntityMapper.fromEntity(
                playerRepository.save(playerEntityMapper.toEntity(player, playerEntity.getGame())));
    }

    @Override
    public void deleteById(@NonNull UUID id) {
        playerRepository.delete(this.getEntityById(id));
    }

    @Override
    public Player getByIdAndGameId(UUID id, UUID gameId) {
        PlayerEntity player = this.getEntityById(id);
        if (!player.getGame().getId().equals(gameId)) {
            throw new PlayerNotPartOfGameException(id, gameId);
        }
        return playerEntityMapper.fromEntity(player);
    }

    private PlayerEntity getEntityById(UUID id) {
        return playerRepository.findById(id).orElseThrow(() -> new NotFoundException("Player", id));
    }

    private GameEntity resolveGameEntity(Player player) {
        return player.getGameId() != null
                ? entityManager.getReference(GameEntity.class, player.getGameId())
                : null;
    }
}
