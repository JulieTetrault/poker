package com.example.poker.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class RankTest {
    @Test
    void givenAllRanks__whenGettingValues__thenReturnValuesRunFromAceOneToKingThirteen() {
        Rank[] ranks = Rank.values();

        int[] values = Arrays.stream(ranks).mapToInt(Rank::getValue).toArray();

        assertThat(values).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13);
    }
}
