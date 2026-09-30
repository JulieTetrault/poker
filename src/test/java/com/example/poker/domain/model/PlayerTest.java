package com.example.poker.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.poker.fixture.CardFixture;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PlayerTest {
    private static final String SOME_PLAYER_NAME = "Alice";
    private static final UUID SOME_PLAYER_ID =
            UUID.fromString("715ba85d-f8a7-4f90-ae14-d974158f2fde");
    private static final UUID SOME_GAME_ID =
            UUID.fromString("f2c7be63-ac5c-4777-8a7c-ea97473818f1");
    private static final Card FIRST_CARD = new CardFixture().withRank(Rank.TWO).build();
    private static final Card SECOND_CARD = new CardFixture().withRank(Rank.THREE).build();
    private static final Card THIRD_CARD = new CardFixture().withRank(Rank.FOUR).build();

    @Test
    void givenPlayerWithCards__whenReceivingMoreCards__thenHandAndValueAreUpdated() {
        Player player = new Player(SOME_PLAYER_ID, SOME_GAME_ID, SOME_PLAYER_NAME);
        player.receiveCards(List.of(FIRST_CARD, SECOND_CARD));

        player.receiveCards(List.of(THIRD_CARD));

        assertThat(player.getHand().getCards())
                .containsExactly(FIRST_CARD, SECOND_CARD, THIRD_CARD);
        assertThat(player.getHandValue()).isEqualTo(9);
    }
}
