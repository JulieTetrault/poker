package com.example.poker.persistence.repository;

import com.example.poker.domain.exception.NotFoundException;
import com.example.poker.domain.model.Game;
import com.example.poker.domain.repository.GameRepository;
import com.example.poker.persistence.entity.GameEntity;
import com.example.poker.persistence.mapper.GameEntityMapper;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class InMemoryGameRepository implements GameRepository {
    private final JPAGameRepository gameRepository;
    private final GameEntityMapper gameEntityMapper;

    public InMemoryGameRepository(
            JPAGameRepository gameRepository, GameEntityMapper gameEntityMapper) {
        this.gameRepository = gameRepository;
        this.gameEntityMapper = gameEntityMapper;
    }

    @Override
    public Game create(Game game) {
        return gameEntityMapper.fromEntity(gameRepository.save(gameEntityMapper.toEntity(game)));
    }

    @Override
    public Game update(Game game) {
        this.getEntityById(game.getId());
        return gameEntityMapper.fromEntity(gameRepository.save(gameEntityMapper.toEntity(game)));
    }

    @Override
    public void deleteById(@NonNull UUID id) {
        gameRepository.delete(this.getEntityById(id));
    }

    public Game getById(UUID id) {
        return gameEntityMapper.fromEntity(this.getEntityById(id));
    }

    private GameEntity getEntityById(UUID id) {
        return gameRepository.findById(id).orElseThrow(() -> new NotFoundException("Game", id));
    }
}
