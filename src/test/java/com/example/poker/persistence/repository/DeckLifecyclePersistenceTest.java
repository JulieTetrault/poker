package com.example.poker.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.poker.domain.model.Game;
import com.example.poker.domain.repository.GameRepository;
import com.example.poker.fixture.PlayerFixture;
import com.example.poker.service.DeckService;
import com.example.poker.service.GameService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class DeckLifecyclePersistenceTest {
    @Autowired private GameService gameService;
    @Autowired private DeckService deckService;
    @Autowired private GameRepository gameRepository;
    @Autowired private JPADeckRepository decks;
    @Autowired private JPAPlayerRepository players;
    @Autowired private EntityManager entityManager;

    @Test
    void givenAttachedDeck__whenReloading__thenPreserveCardsAndRejectReuse() {
        // GIVEN
        Game game = gameService.createGame("Attachment");
        var deck = deckService.createDeck();
        gameService.addDeck(game.getId(), deck.getId());
        reload();
        // WHEN
        Game restored = gameService.getGame(game.getId());
        // THEN
        assertThat(restored.getShoe().getCards()).containsExactlyElementsOf(deck.getCards());
        assertThat(decks.findById(deck.getId()).orElseThrow().getGame().getId())
                .isEqualTo(game.getId());
        assertThatThrownBy(() -> gameService.addDeck(game.getId(), deck.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void givenOwnedRecords__whenDeletingGame__thenDeleteOwnedRecordsAndKeepUnattachedDeck() {
        // GIVEN
        Game game = gameService.createGame("Deletion");
        var attached = deckService.createDeck();
        var unattached = deckService.createDeck();
        gameService.addDeck(game.getId(), attached.getId());
        var player = gameService.addPlayer(game.getId(), "Player");
        reload();
        // WHEN
        gameService.deleteGame(game.getId());
        reload();
        // THEN
        assertThat(decks.existsById(attached.getId())).isFalse();
        assertThat(players.existsById(player.getId())).isFalse();
        assertThat(decks.existsById(unattached.getId())).isTrue();
    }

    @Test
    void givenExhaustedGame__whenAddingDeckAfterReload__thenOnlyNewCardsAreAvailable() {
        // GIVEN
        Game game = gameService.createGame("Exhaustion");
        var first = deckService.createDeck();
        gameService.addDeck(game.getId(), first.getId());
        Game loaded = gameService.getGame(game.getId());
        loaded.dealCards(52, new PlayerFixture().build());
        gameRepository.update(loaded);
        reload();
        assertThat(gameService.getGame(game.getId()).getShoe().getCards()).isEmpty();
        var second = deckService.createDeck();
        // WHEN
        gameService.addDeck(game.getId(), second.getId());
        reload();
        // THEN
        assertThat(gameService.getGame(game.getId()).getShoe().getCards())
                .containsExactlyElementsOf(second.getCards());
        assertThat(decks.findById(first.getId()).orElseThrow().getGame().getId())
                .isEqualTo(game.getId());
    }

    private void reload() {
        entityManager.flush();
        entityManager.clear();
    }
}
