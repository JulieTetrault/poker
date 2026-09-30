package com.example.poker.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.example.poker.domain.model.Card;
import com.example.poker.domain.model.Deck;
import com.example.poker.domain.model.Game;
import com.example.poker.domain.model.Player;
import com.example.poker.domain.model.Shoe;
import com.example.poker.fixture.CardFixture;
import com.example.poker.fixture.DeckEntityFixture;
import com.example.poker.fixture.DeckFixture;
import com.example.poker.fixture.GameEntityFixture;
import com.example.poker.fixture.GameFixture;
import com.example.poker.fixture.PlayerEntityFixture;
import com.example.poker.fixture.PlayerFixture;
import com.example.poker.persistence.entity.DeckEntity;
import com.example.poker.persistence.entity.GameEntity;
import com.example.poker.persistence.entity.PlayerEntity;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GameEntityMapperTest {
    private static final Deck SOME_DECK = new DeckFixture().build();
    private static final DeckEntity SOME_DECK_ENTITY = new DeckEntityFixture().build();
    private static final Player SOME_PLAYER = new PlayerFixture().build();
    private static final PlayerEntity SOME_PLAYER_ENTITY = new PlayerEntityFixture().build();
    private static final Game SOME_GAME =
            new GameFixture()
                    .withDecks(List.of(SOME_DECK))
                    .withPlayers(List.of(SOME_PLAYER))
                    .build();
    private static final GameEntity SOME_GAME_ENTITY =
            new GameEntityFixture()
                    .withDecks(List.of(SOME_DECK_ENTITY))
                    .withPlayers(List.of(SOME_PLAYER_ENTITY))
                    .build();

    @Spy private CardEntityMapper cardEntityMapper = new CardEntityMapper();

    @Mock private DeckEntityMapper deckEntityMapper;

    @Mock private PlayerEntityMapper playerEntityMapper;

    @InjectMocks private GameEntityMapper gameEntityMapper;

    @Test
    void givenGame__whenMappingToEntity__thenReturnGameEntity() {
        given(deckEntityMapper.toEntity(eq(SOME_DECK), any(GameEntity.class)))
                .willReturn(SOME_DECK_ENTITY);
        given(playerEntityMapper.toEntity(eq(SOME_PLAYER), any(GameEntity.class)))
                .willReturn(SOME_PLAYER_ENTITY);

        GameEntity gameEntity = gameEntityMapper.toEntity(SOME_GAME);

        assertThat(gameEntity).isInstanceOf(GameEntity.class);
        assertThat(gameEntity.getId()).isEqualTo(SOME_GAME.getId());
        assertThat(gameEntity.getName()).isEqualTo(SOME_GAME.getName());
        assertThat(gameEntity.getDecks()).containsExactly(SOME_DECK_ENTITY);
        assertThat(gameEntity.getPlayers()).containsExactly(SOME_PLAYER_ENTITY);
        assertThat(gameEntity.getUndealtCards())
                .containsExactlyElementsOf(
                        SOME_GAME.getShoe().getCards().stream()
                                .map(cardEntityMapper::toEntity)
                                .toList());
    }

    @Test
    void givenGameEntity__whenMappingFromEntity__thenReturnGame() {
        given(deckEntityMapper.fromEntity(SOME_DECK_ENTITY)).willReturn(SOME_DECK);
        given(playerEntityMapper.fromEntity(SOME_PLAYER_ENTITY)).willReturn(SOME_PLAYER);

        Game game = gameEntityMapper.fromEntity(SOME_GAME_ENTITY);

        assertThat(game).isInstanceOf(Game.class);
        assertThat(game.getId()).isEqualTo(SOME_GAME_ENTITY.getId());
        assertThat(game.getName()).isEqualTo(SOME_GAME_ENTITY.getName());
        assertThat(game.getShoe().getDecks()).containsExactly(SOME_DECK);
        assertThat(game.getPlayers()).containsExactly(SOME_PLAYER);
    }

    @Test
    void
            givenPartiallyDealtGameDeck__whenMappingBothWays__thenPreserveRemainingOrderAndDuplicates() {
        // GIVEN
        CardEntityMapper cards = new CardEntityMapper();
        GameEntityMapper mapper =
                new GameEntityMapper(
                        new DeckEntityMapper(cards), new PlayerEntityMapper(cards), cards);
        Card first = new CardFixture().build();
        Card second = new CardFixture().build();
        Game original =
                new Game(
                        SOME_GAME.getId(),
                        SOME_GAME.getName(),
                        new Shoe(List.of(SOME_DECK), List.of(second, first, second)),
                        Map.of());
        // WHEN
        Game restored = mapper.fromEntity(mapper.toEntity(original));
        // THEN
        assertThat(restored.getShoe().getCards()).containsExactly(second, first, second);
        assertThat(restored.dealCards(1, new PlayerFixture().build())).containsExactly(second);
        assertThat(restored.getShoe().getCards()).containsExactly(second, first);
    }

    @Test
    void givenExhaustedGameDeck__whenMappingBothWays__thenDoNotReplenishFromDeck() {
        // GIVEN
        CardEntityMapper cards = new CardEntityMapper();
        GameEntityMapper mapper =
                new GameEntityMapper(
                        new DeckEntityMapper(cards), new PlayerEntityMapper(cards), cards);
        Game original =
                new Game(
                        SOME_GAME.getId(),
                        SOME_GAME.getName(),
                        new Shoe(List.of(SOME_DECK), List.of()),
                        Map.of());
        // WHEN
        Game restored = mapper.fromEntity(mapper.toEntity(original));
        // THEN
        assertThat(restored.getShoe().getDecks()).hasSize(1);
        assertThat(restored.dealCards(1, new PlayerFixture().build())).isEmpty();
    }
}
