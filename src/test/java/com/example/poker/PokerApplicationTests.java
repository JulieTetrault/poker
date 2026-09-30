package com.example.poker;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.poker.domain.factory.GameFactory;
import com.example.poker.domain.model.Card;
import com.example.poker.domain.model.Game;
import com.example.poker.domain.model.Suit;
import com.example.poker.fixture.CardFixture;
import com.example.poker.fixture.DeckFixture;
import com.example.poker.fixture.PlayerFixture;
import com.example.poker.persistence.mapper.GameEntityMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PokerApplicationTests {
    @Autowired private GameFactory gameFactory;
    @Autowired private GameEntityMapper gameEntityMapper;

    @Test
    void givenInjectedGameFactory__whenCreatingGame__thenSpringWiringCreatesOwnedEmptyGameDeck() {
        // GIVEN
        String name = "Table";

        // WHEN
        var game = gameFactory.create(name);

        // THEN
        assertThat(game.getName()).isEqualTo(name);
        assertThat(game.getShoe().getDecks()).isEmpty();
    }

    @Test
    void givenCreationAndRestoration__whenDealing__thenCountersAreIndependent() {
        // GIVEN
        Card card = new CardFixture().withSuit(Suit.HEARTS).build();
        Game first = gameFactory.create("First");
        Game second = gameFactory.create("Second");
        first.addDeck(new DeckFixture().withCards(List.of(card, card)).build());
        Game restored = gameEntityMapper.fromEntity(gameEntityMapper.toEntity(first));
        Game restoredAgain = gameEntityMapper.fromEntity(gameEntityMapper.toEntity(first));
        // WHEN
        first.dealCards(1, new PlayerFixture().build());
        restored.dealCards(2, new PlayerFixture().build());
        // THEN
        assertThat(first.getShoe().getUndealtSuitCardsCount().get(Suit.HEARTS)).isEqualTo(1);
        assertThat(second.getShoe().getUndealtSuitCardsCount().get(Suit.HEARTS)).isZero();
        assertThat(restored.getShoe().getUndealtSuitCardsCount().get(Suit.HEARTS)).isZero();
        assertThat(restoredAgain.getShoe().getUndealtSuitCardsCount().get(Suit.HEARTS))
                .isEqualTo(2);
    }
}
