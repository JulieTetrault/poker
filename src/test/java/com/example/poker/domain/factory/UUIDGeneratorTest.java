package com.example.poker.domain.factory;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class UUIDGeneratorTest {
    @Test
    void whenGeneratingIdentifiers__thenReturnDistinctRandomUUIDs() {
        UUIDGenerator generator = new UUIDGenerator();

        UUID firstId = generator.nextId();
        UUID secondId = generator.nextId();

        assertThat(firstId).isNotEqualTo(secondId);
    }
}
