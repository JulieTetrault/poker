package com.example.poker.api.mapper;

import com.example.poker.api.response.GetUndealtSuitCardsResponse;
import com.example.poker.domain.model.Rank;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class GetUndealtSuitCardsResponseMapper {
    public GetUndealtSuitCardsResponse toResponse(Map<Rank, Integer> counts) {
        return new GetUndealtSuitCardsResponse(
                counts.get(Rank.KING),
                counts.get(Rank.QUEEN),
                counts.get(Rank.JACK),
                counts.get(Rank.TEN),
                counts.get(Rank.NINE),
                counts.get(Rank.EIGHT),
                counts.get(Rank.SEVEN),
                counts.get(Rank.SIX),
                counts.get(Rank.FIVE),
                counts.get(Rank.FOUR),
                counts.get(Rank.THREE),
                counts.get(Rank.TWO),
                counts.get(Rank.ACE));
    }
}
