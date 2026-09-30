package com.example.poker.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ShoeTest {
    @Test
    void whenCreating__thenDecksAreEmpty() {
        Shoe shoe = new Shoe();

        assertThat(shoe.getDecks()).isEmpty();
    }
}
