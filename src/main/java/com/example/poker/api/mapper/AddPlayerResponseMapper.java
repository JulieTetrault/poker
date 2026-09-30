package com.example.poker.api.mapper;

import com.example.poker.api.response.AddPlayerResponse;
import com.example.poker.domain.model.Player;
import org.springframework.stereotype.Component;

@Component
public class AddPlayerResponseMapper {
    public AddPlayerResponse toResponse(Player player) {
        return new AddPlayerResponse(player.getId(), player.getName());
    }
}
