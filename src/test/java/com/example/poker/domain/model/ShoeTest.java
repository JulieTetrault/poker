package com.example.poker.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class ShoeTest {
    private static final UUID SOME_SHOE_ID =
            UUID.fromString("715ba85d-f8a7-4f90-ae14-d974158f2fde");
    private static final UUID SOME_GAME_ID =
            UUID.fromString("f2c7be63-ac5c-4777-8a7c-ea97473818f1");

    @Test
    void whenCreating__thenIdentityAndEmptyDecksAreInitialized() {
        Shoe shoe = new Shoe(SOME_SHOE_ID, SOME_GAME_ID);

        assertThat(shoe.getId()).isEqualTo(SOME_SHOE_ID);
        assertThat(shoe.getGameId()).isEqualTo(SOME_GAME_ID);
        assertThat(shoe.getDecks()).isEmpty();
    }
}
