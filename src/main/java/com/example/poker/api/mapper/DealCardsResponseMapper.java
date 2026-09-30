package com.example.poker.api.mapper;

import com.example.poker.api.response.DealCardsResponse;
import com.example.poker.domain.model.Card;
import com.example.poker.domain.model.Game;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DealCardsResponseMapper {
    public DealCardsResponse toResponse(List<Card> dealtCards, Game game) {
        return new DealCardsResponse(dealtCards.size(), game.getShoe().getCards().size());
    }
}
