package com.example.poker.persistence.mapper;

import com.example.poker.domain.model.Game;
import com.example.poker.domain.model.Player;
import com.example.poker.domain.model.Shoe;
import com.example.poker.persistence.entity.GameEntity;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public final class GameEntityMapper {
    private final CardEntityMapper cardEntityMapper;
    private final DeckEntityMapper deckEntityMapper;
    private final PlayerEntityMapper playerEntityMapper;

    public GameEntityMapper(
            DeckEntityMapper deckEntityMapper,
            PlayerEntityMapper playerEntityMapper,
            CardEntityMapper cardEntityMapper) {
        this.cardEntityMapper = cardEntityMapper;
        this.deckEntityMapper = deckEntityMapper;
        this.playerEntityMapper = playerEntityMapper;
    }

    public GameEntity toEntity(Game game) {
        GameEntity gameEntity =
                new GameEntity(
                        game.getId(),
                        game.getName(),
                        game.getShoe().getCards().stream()
                                .map(cardEntityMapper::toEntity)
                                .toList());

        game.getShoe().getDecks().stream()
                .map(deck -> deckEntityMapper.toEntity(deck, gameEntity))
                .forEach(gameEntity::addDeck);

        game.getPlayers().stream()
                .map(player -> playerEntityMapper.toEntity(player, gameEntity))
                .forEach(gameEntity::addPlayer);

        return gameEntity;
    }

    public Game fromEntity(GameEntity entity) {
        return new Game(
                entity.getId(),
                entity.getName(),
                new Shoe(
                        entity.getDecks().stream().map(deckEntityMapper::fromEntity).toList(),
                        entity.getUndealtCards().stream()
                                .map(cardEntityMapper::fromEntity)
                                .toList()),
                entity.getPlayers().stream()
                        .map(playerEntityMapper::fromEntity)
                        .collect(Collectors.toMap(Player::getId, Function.identity())));
    }
}
