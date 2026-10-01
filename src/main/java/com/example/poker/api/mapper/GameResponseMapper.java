package com.example.poker.api.mapper;

import com.example.poker.api.response.AddPlayerResponse;
import com.example.poker.api.response.CreateGameResponse;
import com.example.poker.api.response.DealCardsResponse;
import com.example.poker.api.response.GetPlayerCardResponse;
import com.example.poker.api.response.GetPlayerHandValueResponse;
import com.example.poker.api.response.GetUndealtCardsResponse;
import com.example.poker.api.response.GetUndealtSuitCardsCountResponse;
import com.example.poker.api.response.GetUndealtSuitCardsResponse;
import com.example.poker.domain.model.Card;
import com.example.poker.domain.model.Game;
import com.example.poker.domain.model.Player;
import com.example.poker.domain.model.Rank;
import com.example.poker.domain.model.Suit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class GameResponseMapper {

    public CreateGameResponse toCreateGameResponse(Game game) {
        return new CreateGameResponse(game.getId(), game.getName());
    }

    public DealCardsResponse toDealCardsResponse(List<Card> dealtCards, Game game) {
        return new DealCardsResponse(dealtCards.size(), game.getShoe().getCards().size());
    }

    public GetUndealtCardsResponse toGetUndealtCardsResponse(Game game) {
        Map<Suit, Map<Rank, Integer>> counts = game.getShoe().getUndealtCardCounts();
        return new GetUndealtCardsResponse(
                toGetUndealtSuitCardsResponse(counts.get(Suit.HEARTS)),
                toGetUndealtSuitCardsResponse(counts.get(Suit.SPADES)),
                toGetUndealtSuitCardsResponse(counts.get(Suit.CLUBS)),
                toGetUndealtSuitCardsResponse(counts.get(Suit.DIAMONDS)));
    }

    public GetUndealtSuitCardsCountResponse toGetUndealtSuitCardsCountResponse(Game game) {
        Map<Suit, Integer> counts = game.getShoe().getUndealtSuitCardsCount();
        return new GetUndealtSuitCardsCountResponse(
                counts.get(Suit.HEARTS),
                counts.get(Suit.SPADES),
                counts.get(Suit.CLUBS),
                counts.get(Suit.DIAMONDS));
    }

    public AddPlayerResponse toAddPlayerResponse(Player player) {
        return new AddPlayerResponse(player.getId(), player.getName());
    }

    public List<GetPlayerCardResponse> toGetPlayerCardsResponse(List<Card> cards) {
        return cards.stream().map(this::toGetPlayerCardResponse).toList();
    }

    public List<GetPlayerHandValueResponse> toGetPlayersHandValueResponse(List<Player> players) {
        return players.stream().map(this::toGetPlayerHandValueResponse).toList();
    }

    private GetUndealtSuitCardsResponse toGetUndealtSuitCardsResponse(Map<Rank, Integer> counts) {
        Map<String, Integer> responseCounts = new LinkedHashMap<>();
        counts.forEach((rank, count) -> responseCounts.put(rank.getLabel(), count));
        return new GetUndealtSuitCardsResponse(responseCounts);
    }

    private GetPlayerCardResponse toGetPlayerCardResponse(Card card) {
        return new GetPlayerCardResponse(card.suit().getLabel(), card.rank().getLabel());
    }

    private GetPlayerHandValueResponse toGetPlayerHandValueResponse(Player player) {
        return new GetPlayerHandValueResponse(
                player.getId(), player.getName(), player.getHandValue());
    }
}
