package com.example.poker.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.example.poker.domain.exception.NotFoundException;
import com.example.poker.domain.model.Deck;
import com.example.poker.fixture.DeckEntityFixture;
import com.example.poker.fixture.DeckFixture;
import com.example.poker.fixture.GameEntityFixture;
import com.example.poker.persistence.entity.DeckEntity;
import com.example.poker.persistence.entity.GameEntity;
import com.example.poker.persistence.mapper.DeckEntityMapper;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InMemoryDeckRepositoryTest {
    private static final GameEntity SOME_GAME_ENTITY = new GameEntityFixture().build();
    private static final Deck SOME_DECK = new DeckFixture().build();
    private static final Deck SOME_DECK_ATTACHED_TO_GAME =
            new DeckFixture().withGameId(SOME_GAME_ENTITY.getId()).build();
    private static final Deck SOME_PERSISTED_DECK = new DeckFixture().build();
    private static final DeckEntity SOME_DECK_ENTITY = new DeckEntityFixture().build();
    private static final DeckEntity SOME_PERSISTED_DECK_ENTITY = new DeckEntityFixture().build();

    @Mock private JPADeckRepository deckRepository;

    @Mock private DeckEntityMapper deckEntityMapper;

    @Mock private EntityManager entityManager;

    @InjectMocks private InMemoryDeckRepository inMemoryDeckRepository;

    @Test
    void whenCreating__thenReturnPersistedDeck() {
        given(deckEntityMapper.toEntity(SOME_DECK)).willReturn(SOME_DECK_ENTITY);
        given(deckRepository.save(SOME_DECK_ENTITY)).willReturn(SOME_PERSISTED_DECK_ENTITY);
        given(deckEntityMapper.fromEntity(SOME_PERSISTED_DECK_ENTITY))
                .willReturn(SOME_PERSISTED_DECK);

        Deck deck = inMemoryDeckRepository.create(SOME_DECK);

        assertThat(deck).isSameAs(SOME_PERSISTED_DECK);
        verify(deckRepository).save(SOME_DECK_ENTITY);
    }

    @Test
    void
            givenExistingDeckWithGameReference__whenUpdating__thenReturnPersistedDeckWithGameReference() {
        given(deckRepository.findById(SOME_DECK_ATTACHED_TO_GAME.getId()))
                .willReturn(Optional.of(SOME_DECK_ENTITY));
        given(entityManager.getReference(GameEntity.class, SOME_GAME_ENTITY.getId()))
                .willReturn(SOME_GAME_ENTITY);
        given(deckEntityMapper.toEntity(SOME_DECK_ATTACHED_TO_GAME, SOME_GAME_ENTITY))
                .willReturn(SOME_DECK_ENTITY);
        given(deckRepository.save(SOME_DECK_ENTITY)).willReturn(SOME_PERSISTED_DECK_ENTITY);
        given(deckEntityMapper.fromEntity(SOME_PERSISTED_DECK_ENTITY))
                .willReturn(SOME_PERSISTED_DECK);

        Deck deck = inMemoryDeckRepository.update(SOME_DECK_ATTACHED_TO_GAME);

        assertThat(deck).isSameAs(SOME_PERSISTED_DECK);
        verify(deckRepository).save(SOME_DECK_ENTITY);
    }

    @Test
    void
            givenExistingDeckWithoutGameReference__whenUpdating__thenReturnPersistedDeckWithoutGameReference() {
        given(deckRepository.findById(SOME_DECK.getId())).willReturn(Optional.of(SOME_DECK_ENTITY));
        given(deckEntityMapper.toEntity(SOME_DECK, null)).willReturn(SOME_DECK_ENTITY);
        given(deckRepository.save(SOME_DECK_ENTITY)).willReturn(SOME_PERSISTED_DECK_ENTITY);
        given(deckEntityMapper.fromEntity(SOME_PERSISTED_DECK_ENTITY))
                .willReturn(SOME_PERSISTED_DECK);

        Deck deck = inMemoryDeckRepository.update(SOME_DECK);

        assertThat(deck).isSameAs(SOME_PERSISTED_DECK);
        verify(deckRepository).save(SOME_DECK_ENTITY);
        verifyNoInteractions(entityManager);
    }

    @Test
    void givenMissingDeck__whenUpdating__thenThrowNotFoundException() {
        given(deckRepository.findById(SOME_DECK.getId())).willReturn(Optional.empty());

        var exception = assertThatThrownBy(() -> inMemoryDeckRepository.update(SOME_DECK));

        exception
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Deck not found: " + SOME_DECK.getId());
        verify(deckRepository).findById(SOME_DECK.getId());
        verifyNoMoreInteractions(deckRepository);
        verifyNoInteractions(deckEntityMapper, entityManager);
    }
}
