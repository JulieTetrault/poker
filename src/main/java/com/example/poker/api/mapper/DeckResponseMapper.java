package com.example.poker.api.mapper;

import com.example.poker.api.response.CreateDeckResponse;
import com.example.poker.domain.model.Deck;
import org.springframework.stereotype.Component;

@Component
public class DeckResponseMapper {
    public CreateDeckResponse toCreateDeckResponse(Deck deck) {
        return new CreateDeckResponse(deck.getId());
    }
}
