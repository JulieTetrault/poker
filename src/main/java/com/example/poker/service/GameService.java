package com.example.poker.service;

import com.example.poker.domain.factory.GameFactory;
import com.example.poker.domain.model.Card;
import com.example.poker.domain.model.Deck;
import com.example.poker.domain.model.Game;
import com.example.poker.domain.model.Player;
import com.example.poker.domain.repository.GameRepository;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GameService {
    private final GameFactory gameFactory;
    private final GameRepository gameRepository;
    private final DeckService deckService;
    private final PlayerService playerService;

    public GameService(
            GameFactory gameFactory,
            GameRepository gameRepository,
            DeckService deckService,
            PlayerService playerService) {
        this.gameFactory = gameFactory;
        this.gameRepository = gameRepository;
        this.deckService = deckService;
        this.playerService = playerService;
    }

    public Game createGame(String gameName) {
        return gameRepository.create(gameFactory.create(gameName));
    }

    public void deleteGame(UUID gameId) {
        gameRepository.deleteById(gameId);
    }

    public Game getGame(UUID gameId) {
        return gameRepository.getById(gameId);
    }

    public void addDeck(UUID gameId, UUID deckId) {
        Game game = gameRepository.getById(gameId);
        Deck deck = deckService.attachDeckToGame(gameId, deckId);

        game.addDeck(deck);
        gameRepository.update(game);
    }

    public Player addPlayer(UUID gameId, String playerName) {
        Game game = gameRepository.getById(gameId);
        Player player = playerService.createPlayer(gameId, playerName);

        game.addPlayer(player);
        gameRepository.update(game);

        return player;
    }

    public void removePlayer(UUID gameId, UUID playerId) {
        Game game = gameRepository.getById(gameId);
        Player player = playerService.getPlayer(gameId, playerId);

        game.removePlayer(player);
        gameRepository.update(game);
    }

    public List<Card> getPlayerCards(UUID gameId, UUID playerId) {
        Game game = gameRepository.getById(gameId);
        return playerService.getPlayer(game.getId(), playerId).getCards();
    }

    public List<Player> getPlayers(UUID gameId) {
        Game game = gameRepository.getById(gameId);
        return game.getPlayers().stream()
                .sorted(Comparator.comparingInt(Player::getHandValue).reversed().thenComparing(Player::getName))
                .toList();
    }

    public List<Card> dealCards(int cardCount, UUID gameId, UUID playerId) {
        Game game = gameRepository.getById(gameId);
        Player player = playerService.getPlayer(game.getId(), playerId);

        List<Card> dealtCards = game.dealCards(cardCount, player);
        gameRepository.update(game);

        return dealtCards;
    }

    public void shuffleCards(UUID gameId) {
        Game game = gameRepository.getById(gameId);
        game.getShoe().shuffle();
        gameRepository.update(game);
    }
}
