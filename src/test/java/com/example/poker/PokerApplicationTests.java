package com.example.poker;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.poker.domain.factory.GameFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PokerApplicationTests {
    @Autowired private GameFactory gameFactory;

    @Test
    void givenInjectedGameFactory__whenCreatingGame__thenSpringWiringCreatesOwnedEmptyShoe() {
        // GIVEN
        String name = "Table";

        // WHEN
        var game = gameFactory.create(name);

        // THEN
        assertThat(game.getName()).isEqualTo(name);
        assertThat(game.getShoe().getDecks()).isEmpty();
    }
}
