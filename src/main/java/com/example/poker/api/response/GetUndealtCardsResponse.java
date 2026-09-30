package com.example.poker.api.response;

public record GetUndealtCardsResponse(
        GetUndealtSuitCardsResponse hearts,
        GetUndealtSuitCardsResponse spades,
        GetUndealtSuitCardsResponse clubs,
        GetUndealtSuitCardsResponse diamonds) {}
