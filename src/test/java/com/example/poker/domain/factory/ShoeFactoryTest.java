package com.example.poker.domain.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.example.poker.domain.model.Shoe;
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
class ShoeFactoryTest {
    private static final UUID SOME_SHOE_ID =
            UUID.fromString("715ba85d-f8a7-4f90-ae14-d974158f2fde");
    private static final UUID SOME_GAME_ID =
            UUID.fromString("f2c7be63-ac5c-4777-8a7c-ea97473818f1");

    @Mock private IdGenerator idGenerator;

    @InjectMocks private ShoeFactory shoeFactory;

    @BeforeEach
    void setUp() {
        given(idGenerator.nextId()).willReturn(SOME_SHOE_ID);
    }

    @Nested
    @DisplayName("Creating a shoe")
    class Creation {
        @Test
        void whenCreating__thenShoeHasGeneratedIdentityAndEmptyDecks() {
            Shoe shoe = shoeFactory.create(SOME_GAME_ID);

            assertThat(shoe.getId()).isEqualTo(SOME_SHOE_ID);
            assertThat(shoe.getGameId()).isEqualTo(SOME_GAME_ID);
            assertThat(shoe.getDecks()).isEmpty();
        }
    }
}
