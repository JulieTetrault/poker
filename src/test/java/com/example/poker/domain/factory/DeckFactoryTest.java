package com.example.poker.domain.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.example.poker.domain.model.Deck;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeckFactoryTest {
    private static final UUID SOME_DECK_ID = UUID.fromString("715ba85d-f8a7-4f90-ae14-d974158f2fde");

    @Mock
    private IdGenerator idGenerator;

    @InjectMocks
    private DeckFactory deckFactory;

    @BeforeEach
    void setUp() {
        given(idGenerator.nextId()).willReturn(SOME_DECK_ID);
    }

    @Nested
    @DisplayName("Creating a deck")
    class Creation {
        @Test
        void whenCreating__thenDeckHasGeneratedIdentityAndAllFiftyTwoCards() {
            Deck deck = deckFactory.create();

            assertThat(deck.getId()).isEqualTo(SOME_DECK_ID);
            assertThat(deck.getGameId()).isNull();
        }
    }
}
