package com.example.poker.api.mapper;

import com.example.poker.api.response.GetPlayerCardResponse;
import com.example.poker.domain.model.Card;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class GetPlayerCardResponseMapper {
    public GetPlayerCardResponse toResponse(Card card) {
        String rank =
                switch (card.rank()) {
                    case ACE -> "Ace";
                    case JACK -> "Jack";
                    case QUEEN -> "Queen";
                    case KING -> "King";
                    default -> Integer.toString(card.rank().getValue());
                };
        return new GetPlayerCardResponse(card.suit().name().toLowerCase(Locale.ROOT), rank);
    }

    public List<GetPlayerCardResponse> toResponse(List<Card> cards) {
        return cards.stream().map(this::toResponse).toList();
    }
}
