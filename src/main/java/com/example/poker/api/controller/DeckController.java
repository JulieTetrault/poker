package com.example.poker.api.controller;

import com.example.poker.api.mapper.DeckResponseMapper;
import com.example.poker.api.response.CreateDeckResponse;
import com.example.poker.service.DeckService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/decks")
public class DeckController {
    private final DeckService deckService;
    private final DeckResponseMapper deckResponseMapper;

    public DeckController(DeckService deckService, DeckResponseMapper deckResponseMapper) {
        this.deckService = deckService;
        this.deckResponseMapper = deckResponseMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateDeckResponse createDeck() {
        return deckResponseMapper.toCreateDeckResponse(deckService.createDeck());
    }
}
