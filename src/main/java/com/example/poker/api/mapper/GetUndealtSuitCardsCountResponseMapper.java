package com.example.poker.api.mapper;

import com.example.poker.api.response.GetUndealtSuitCardsCountResponse;
import com.example.poker.domain.model.Game;
import com.example.poker.domain.model.Suit;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class GetUndealtSuitCardsCountResponseMapper {
    public GetUndealtSuitCardsCountResponse toResponse(Game game) {
        Map<Suit, Integer> counts = game.getShoe().getUndealtSuitCardsCount();
        return new GetUndealtSuitCardsCountResponse(
                counts.get(Suit.HEARTS),
                counts.get(Suit.SPADES),
                counts.get(Suit.CLUBS),
                counts.get(Suit.DIAMONDS));
    }
}
