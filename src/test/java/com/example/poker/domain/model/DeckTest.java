package com.example.poker.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DeckTest {
    private static final UUID SOME_DECK_ID =
            UUID.fromString("715ba85d-f8a7-4f90-ae14-d974158f2fde");
    private static final UUID SOME_SHOE_ID =
            UUID.fromString("f2c7be63-ac5c-4777-8a7c-ea97473818f1");

    @Test
    void givenDeck__whenAssigningShoeId__thenShoeIdIsUpdated() {
        Deck deck = new Deck(SOME_DECK_ID, List.of());

        deck.setShoeId(SOME_SHOE_ID);

        assertThat(deck.getShoeId()).isEqualTo(SOME_SHOE_ID);
    }
}
