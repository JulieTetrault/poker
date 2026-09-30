package com.example.poker.api.mapper;

import com.example.poker.api.response.GetUndealtCardsResponse;
import com.example.poker.domain.model.Game;
import com.example.poker.domain.model.Rank;
import com.example.poker.domain.model.Suit;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class GetUndealtCardsResponseMapper {
    private final GetUndealtSuitCardsResponseMapper suitCardsResponseMapper;

    public GetUndealtCardsResponseMapper(
            GetUndealtSuitCardsResponseMapper suitCardsResponseMapper) {
        this.suitCardsResponseMapper = suitCardsResponseMapper;
    }

    public GetUndealtCardsResponse toResponse(Game game) {
        Map<Suit, Map<Rank, Integer>> counts = game.getShoe().getUndealtCardCounts();
        return new GetUndealtCardsResponse(
                suitCardsResponseMapper.toResponse(counts.get(Suit.HEARTS)),
                suitCardsResponseMapper.toResponse(counts.get(Suit.SPADES)),
                suitCardsResponseMapper.toResponse(counts.get(Suit.CLUBS)),
                suitCardsResponseMapper.toResponse(counts.get(Suit.DIAMONDS)));
    }
}
