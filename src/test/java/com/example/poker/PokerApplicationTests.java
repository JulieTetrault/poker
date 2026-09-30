package com.example.poker;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.poker.domain.factory.GameFactory;
import com.example.poker.domain.factory.ShoeFactory;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PokerApplicationTests {
    @Autowired private GameFactory gameFactory;
    @Autowired private ShoeFactory shoeFactory;

    @Test
    void givenInjectedFactories__whenCreatingGameAndShoe__thenSpringWiringCreatesOwnedEmptyShoe() {
        // GIVEN
        String name = "Table";
        UUID gameId = new UUID(0, 1);

        // WHEN
        var game = gameFactory.create(name);
        var shoe = shoeFactory.create(gameId);

        // THEN
        assertThat(game.getName()).isEqualTo(name);
        assertThat(game.getShoe().getGameId()).isEqualTo(game.getId());
        assertThat(game.getShoe().getDecks()).isEmpty();
        assertThat(shoe.getGameId()).isEqualTo(gameId);
        assertThat(shoe.getDecks()).isEmpty();
    }
}
