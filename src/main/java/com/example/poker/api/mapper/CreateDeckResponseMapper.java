package com.example.poker.api.mapper;

import com.example.poker.api.response.CreateDeckResponse;
import com.example.poker.domain.model.Deck;
import org.springframework.stereotype.Component;

@Component
public class CreateDeckResponseMapper {
    public CreateDeckResponse toResponse(Deck deck) {
        return new CreateDeckResponse(deck.getId());
    }
}
