package com.example.poker.api.controller;

import com.example.poker.api.mapper.GameResponseMapper;
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
import com.example.poker.api.validator.UUIDValidator;
import com.example.poker.domain.model.Card;
import com.example.poker.service.GameService;
import jakarta.validation.Valid;
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
    private final GameResponseMapper gameResponseMapper;
    private final UUIDValidator uuidValidator;

    public GameController(
            GameService gameService,
            UUIDValidator uuidValidator,
            GameResponseMapper gameResponseMapper) {
        this.gameService = gameService;
        this.uuidValidator = uuidValidator;
        this.gameResponseMapper = gameResponseMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateGameResponse createGame(@Valid @RequestBody CreateGameRequest request) {
        return gameResponseMapper.toCreateGameResponse(gameService.createGame(request.name()));
    }

    @DeleteMapping("/{gameId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGame(@PathVariable UUID gameId) {
        gameService.deleteGame(gameId);
    }

    @PostMapping("/{gameId}/decks")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addDeck(@PathVariable UUID gameId, @Valid @RequestBody AddDeckRequest request) {
        UUID deckId = uuidValidator.validate(request.deckId(), "deckId");
        gameService.addDeck(gameId, deckId);
    }

    @GetMapping("/{gameId}/decks/cards")
    public GetUndealtCardsResponse countCards(@PathVariable UUID gameId) {
        return gameResponseMapper.toGetUndealtCardsResponse(gameService.getGame(gameId));
    }

    @GetMapping("/{gameId}/decks/suits/cards")
    public GetUndealtSuitCardsCountResponse countSuits(@PathVariable UUID gameId) {
        return gameResponseMapper.toGetUndealtSuitCardsCountResponse(gameService.getGame(gameId));
    }

    @PostMapping("/{gameId}/decks/shuffle")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void shuffleShoe(@PathVariable UUID gameId) {
        gameService.shuffleCards(gameId);
    }

    @PostMapping("/{gameId}/players")
    @ResponseStatus(HttpStatus.CREATED)
    public AddPlayerResponse addPlayer(
            @PathVariable UUID gameId, @Valid @RequestBody AddPlayerRequest request) {
        return gameResponseMapper.toAddPlayerResponse(
                gameService.addPlayer(gameId, request.name().trim()));
    }

    @GetMapping("/{gameId}/players")
    public List<GetPlayerHandValueResponse> listPlayers(@PathVariable UUID gameId) {
        return gameResponseMapper.toGetPlayersHandValueResponse(gameService.getPlayers(gameId));
    }

    @DeleteMapping("/{gameId}/players/{playerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removePlayer(@PathVariable UUID gameId, @PathVariable UUID playerId) {
        gameService.removePlayer(gameId, playerId);
    }

    @GetMapping("/{gameId}/players/{playerId}/cards")
    public List<GetPlayerCardResponse> getHand(
            @PathVariable UUID gameId, @PathVariable UUID playerId) {
        return gameResponseMapper.toGetPlayerCardsResponse(
                gameService.getPlayerCards(gameId, playerId));
    }

    @PostMapping("/{gameId}/players/{playerId}/cards/deal")
    public DealCardsResponse dealCards(
            @PathVariable UUID gameId,
            @PathVariable UUID playerId,
            @Valid @RequestBody DealCardsRequest request) {
        List<Card> dealtCards = gameService.dealCards(request.count(), gameId, playerId);
        return gameResponseMapper.toDealCardsResponse(dealtCards, gameService.getGame(gameId));
    }
}
