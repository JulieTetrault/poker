package com.example.poker.api.mapper;

import com.example.poker.api.response.GetPlayerHandValueResponse;
import com.example.poker.domain.model.Player;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class GetPlayerHandValueResponseMapper {
    public GetPlayerHandValueResponse toResponse(Player player) {
        return new GetPlayerHandValueResponse(
                player.getId(), player.getName(), player.getHandValue());
    }

    public List<GetPlayerHandValueResponse> toResponse(List<Player> players) {
        return players.stream().map(this::toResponse).toList();
    }
}
