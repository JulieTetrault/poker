package com.example.poker.api.response;

import java.util.UUID;

public record GetPlayerHandValueResponse(UUID id, String name, long handValue) {}
