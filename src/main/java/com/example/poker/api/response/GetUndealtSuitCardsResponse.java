package com.example.poker.api.response;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record GetUndealtSuitCardsResponse(@JsonValue Map<String, Integer> counts) {
    public GetUndealtSuitCardsResponse {
        counts = Collections.unmodifiableMap(new LinkedHashMap<>(counts));
    }
}
