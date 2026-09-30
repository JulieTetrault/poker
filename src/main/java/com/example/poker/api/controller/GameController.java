package com.example.poker.api.controller;

import com.example.poker.api.mapper.AddPlayerResponseMapper;
import com.example.poker.api.mapper.CreateGameResponseMapper;
import com.example.poker.api.mapper.DealCardsResponseMapper;
import com.example.poker.api.mapper.GetPlayerCardResponseMapper;
import com.example.poker.api.mapper.GetPlayerHandValueResponseMapper;
import com.example.poker.api.mapper.GetUndealtCardsResponseMapper;
import com.example.poker.api.mapper.GetUndealtSuitCardsCountResponseMapper;
import com.example.poker.api.request.AddDeckRequest;
import com.example.poker.api.request.AddPlayerRequest;
import com.example.poker.api.request.CreateGameRequest;
import com.example.poker.api.request.DealCardsRequest;
import com.example.poker.api.response.AddPlayerResponse;
import com.example.poker.api.response.CreateGameResponse;
import com.example.poker.api.response.DealCardsResponse;
import com.example.poker.api.response.GetPlayerCardResponse;
import com.example.poker.api.response.GetPlayerHandValueResponse;
import com.example.poker.api.response.GetUndealtCardsResponse;
import com.example.poker.api.response.GetUndealtSuitCardsCountResponse;
import com.example.poker.domain.model.Card;
import com.example.poker.service.GameService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/games")
public class GameController {
    private final GameService gameService;
    private final CreateGameResponseMapper createGameResponseMapper;
    private final AddPlayerResponseMapper addPlayerResponseMapper;
    private final GetPlayerCardResponseMapper getPlayerCardResponseMapper;
    private final GetPlayerHandValueResponseMapper getPlayerHandValueResponseMapper;
    private final DealCardsResponseMapper dealCardsResponseMapper;
    private final GetUndealtCardsResponseMapper getUndealtCardsResponseMapper;
    private final GetUndealtSuitCardsCountResponseMapper getUndealtSuitCardsCountResponseMapper;

    public GameController(
            GameService gameService,
            CreateGameResponseMapper createGameResponseMapper,
            AddPlayerResponseMapper addPlayerResponseMapper,
            GetPlayerCardResponseMapper getPlayerCardResponseMapper,
            GetPlayerHandValueResponseMapper getPlayerHandValueResponseMapper,
            DealCardsResponseMapper dealCardsResponseMapper,
            GetUndealtCardsResponseMapper getUndealtCardsResponseMapper,
            GetUndealtSuitCardsCountResponseMapper getUndealtSuitCardsCountResponseMapper) {
        this.gameService = gameService;
        this.createGameResponseMapper = createGameResponseMapper;
        this.addPlayerResponseMapper = addPlayerResponseMapper;
        this.getPlayerCardResponseMapper = getPlayerCardResponseMapper;
        this.getPlayerHandValueResponseMapper = getPlayerHandValueResponseMapper;
        this.dealCardsResponseMapper = dealCardsResponseMapper;
        this.getUndealtCardsResponseMapper = getUndealtCardsResponseMapper;
        this.getUndealtSuitCardsCountResponseMapper = getUndealtSuitCardsCountResponseMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateGameResponse createGame(@RequestBody CreateGameRequest request) {
        return createGameResponseMapper.toResponse(gameService.createGame(request.name()));
    }

    @DeleteMapping("/{gameId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGame(@PathVariable UUID gameId) {
        gameService.deleteGame(gameId);
    }

    @PostMapping("/{gameId}/decks")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addDeck(@PathVariable UUID gameId, @RequestBody AddDeckRequest request) {
        gameService.addDeck(gameId, request.deckId());
    }

    @GetMapping("/{gameId}/decks/cards")
    public GetUndealtCardsResponse countCards(@PathVariable UUID gameId) {
        return getUndealtCardsResponseMapper.toResponse(gameService.getGame(gameId));
    }

    @GetMapping("/{gameId}/decks/suits/cards")
    public GetUndealtSuitCardsCountResponse countSuits(@PathVariable UUID gameId) {
        return getUndealtSuitCardsCountResponseMapper.toResponse(gameService.getGame(gameId));
    }

    @PostMapping("/{gameId}/decks/shuffle")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void shuffleShoe(@PathVariable UUID gameId) {
        gameService.shuffleCards(gameId);
    }

    @PostMapping("/{gameId}/players")
    @ResponseStatus(HttpStatus.CREATED)
    public AddPlayerResponse addPlayer(
            @PathVariable UUID gameId, @RequestBody AddPlayerRequest request) {
        return addPlayerResponseMapper.toResponse(
                gameService.addPlayer(gameId, request.name().trim()));
    }

    @GetMapping("/{gameId}/players")
    public List<GetPlayerHandValueResponse> listPlayers(@PathVariable UUID gameId) {
        return getPlayerHandValueResponseMapper.toResponse(gameService.getPlayers(gameId));
    }

    @DeleteMapping("/{gameId}/players/{playerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removePlayer(@PathVariable UUID gameId, @PathVariable UUID playerId) {
        gameService.removePlayer(gameId, playerId);
    }

    @GetMapping("/{gameId}/players/{playerId}/cards")
    public List<GetPlayerCardResponse> getHand(
            @PathVariable UUID gameId, @PathVariable UUID playerId) {
        return getPlayerCardResponseMapper.toResponse(gameService.getPlayerCards(gameId, playerId));
    }

    @PostMapping("/{gameId}/players/{playerId}/cards/deal")
    public DealCardsResponse dealCards(
            @PathVariable UUID gameId,
            @PathVariable UUID playerId,
            @RequestBody DealCardsRequest request) {
        List<Card> dealtCards = gameService.dealCards(request.count(), gameId, playerId);
        return dealCardsResponseMapper.toResponse(dealtCards, gameService.getGame(gameId));
    }
}
