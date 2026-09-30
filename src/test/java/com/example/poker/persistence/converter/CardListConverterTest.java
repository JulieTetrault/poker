package com.example.poker.persistence.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.poker.domain.model.Rank;
import com.example.poker.domain.model.Suit;
import com.example.poker.persistence.entity.CardEntity;
import java.util.List;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;

class CardListConverterTest {
    private final CardListConverter converter = new CardListConverter();

    @Test
    void givenRepeatedCardValues__whenConvertingBothWays__thenPreservesOrderAndMultiplicity() {
        CardEntity first = new CardEntity(Suit.HEARTS, Rank.ACE);
        CardEntity second = new CardEntity(Suit.SPADES, Rank.QUEEN);
        List<CardEntity> cards = List.of(second, first, second);

        String json = converter.convertToDatabaseColumn(cards);
        var restored = converter.convertToEntityAttribute(json);

        assertThat(restored).containsExactly(second, first, second);
        assertThat(json)
                .startsWith("[")
                .endsWith("]")
                .contains("\"suit\":\"HEARTS\"", "\"rank\":\"ACE\"")
                .doesNotContain("\"id\"", "\"deckId\"");
    }

    @Test
    void givenEmptyList__whenConvertingBothWays__thenPreservesEmptyArray() {
        List<CardEntity> cards = List.of();

        String json = converter.convertToDatabaseColumn(cards);
        var restored = converter.convertToEntityAttribute(json);

        assertThat(json).isEqualTo("[]");
        assertThat(restored).isEmpty();
    }

    @Test
    void givenNull__whenConvertingBothWays__thenPreservesNull() {
        List<CardEntity> cards = null;
        String stored = null;

        String json = converter.convertToDatabaseColumn(cards);
        var restored = converter.convertToEntityAttribute(stored);

        assertThat(json).isNull();
        assertThat(restored).isNull();
    }

    @Test
    void givenMalformedJson__whenReading__thenRejectsCorruptedState() {
        String json = "not-json";

        var action = (ThrowingCallable) () -> converter.convertToEntityAttribute(json);

        assertThatThrownBy(action).isInstanceOf(JacksonException.class);
    }
}
