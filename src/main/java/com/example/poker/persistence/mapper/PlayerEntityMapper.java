package com.example.poker.persistence.mapper;

import com.example.poker.domain.model.Player;
import com.example.poker.persistence.entity.GameEntity;
import com.example.poker.persistence.entity.PlayerEntity;
import org.springframework.stereotype.Component;

@Component
public final class PlayerEntityMapper {
    private final CardEntityMapper cardEntityMapper;

    public PlayerEntityMapper(CardEntityMapper cardEntityMapper) {
        this.cardEntityMapper = cardEntityMapper;
    }

    public PlayerEntity toEntity(Player player, GameEntity gameEntity) {
        return new PlayerEntity(
                player.getId(),
                gameEntity,
                player.getName(),
                player.getCards().stream().map(cardEntityMapper::toEntity).toList());
    }

    public Player fromEntity(PlayerEntity entity) {
        return new Player(
                entity.getId(),
                entity.getGame().getId(),
                entity.getName(),
                entity.getCards().stream().map(cardEntityMapper::fromEntity).toList());
    }
}
