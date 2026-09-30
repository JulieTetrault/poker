package com.example.poker.api.mapper;

import com.example.poker.api.response.CreateGameResponse;
import com.example.poker.domain.model.Game;
import org.springframework.stereotype.Component;

@Component
public class CreateGameResponseMapper {
    public CreateGameResponse toResponse(Game game) {
        return new CreateGameResponse(game.getId(), game.getName());
    }
}
