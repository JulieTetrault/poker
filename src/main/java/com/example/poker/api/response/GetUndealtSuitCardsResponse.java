package com.example.poker.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GetUndealtSuitCardsResponse(
        int king,
        int queen,
        int jack,
        @JsonProperty("10") int ten,
        @JsonProperty("9") int nine,
        @JsonProperty("8") int eight,
        @JsonProperty("7") int seven,
        @JsonProperty("6") int six,
        @JsonProperty("5") int five,
        @JsonProperty("4") int four,
        @JsonProperty("3") int three,
        @JsonProperty("2") int two,
        int ace) {}
