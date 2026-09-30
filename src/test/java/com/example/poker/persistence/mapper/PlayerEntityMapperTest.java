package com.example.poker.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.example.poker.domain.model.Card;
import com.example.poker.domain.model.Player;
import com.example.poker.fixture.CardEntityFixture;
import com.example.poker.fixture.CardFixture;
import com.example.poker.fixture.GameEntityFixture;
import com.example.poker.fixture.PlayerEntityFixture;
import com.example.poker.fixture.PlayerFixture;
import com.example.poker.persistence.entity.CardEntity;
import com.example.poker.persistence.entity.GameEntity;
import com.example.poker.persistence.entity.PlayerEntity;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlayerEntityMapperTest {
    private static final GameEntity SOME_GAME_ENTITY = new GameEntityFixture().build();
    private static final Card SOME_CARD = new CardFixture().build();
    private static final CardEntity SOME_CARD_ENTITY = new CardEntityFixture().build();
    private static final Player SOME_PLAYER =
            new PlayerFixture().withCards(List.of(SOME_CARD)).build();
    private static final PlayerEntity SOME_PLAYER_ENTITY =
            new PlayerEntityFixture().withCards(List.of(SOME_CARD_ENTITY)).build();

    @Mock private CardEntityMapper cardEntityMapper;

    @InjectMocks private PlayerEntityMapper playerEntityMapper;

    @Test
    void givenPlayer__whenMappingToEntity__thenReturnPlayerEntity() {
        given(cardEntityMapper.toEntity(SOME_CARD)).willReturn(SOME_CARD_ENTITY);

        PlayerEntity playerEntity = playerEntityMapper.toEntity(SOME_PLAYER, SOME_GAME_ENTITY);

        assertThat(playerEntity).isInstanceOf(PlayerEntity.class);
        assertThat(playerEntity.getId()).isEqualTo(SOME_PLAYER.getId());
        assertThat(playerEntity.getGame()).isEqualTo(SOME_GAME_ENTITY);
        assertThat(playerEntity.getName()).isEqualTo(SOME_PLAYER.getName());
        assertThat(playerEntity.getCards()).containsExactly(SOME_CARD_ENTITY);
    }

    @Test
    void givenPlayerEntity__whenMappingFromEntity__thenReturnPlayer() {
        given(cardEntityMapper.fromEntity(SOME_CARD_ENTITY)).willReturn(SOME_CARD);

        Player player = playerEntityMapper.fromEntity(SOME_PLAYER_ENTITY);

        assertThat(player).isInstanceOf(Player.class);
        assertThat(player.getId()).isEqualTo(SOME_PLAYER_ENTITY.getId());
        assertThat(player.getGameId()).isEqualTo(SOME_PLAYER_ENTITY.getGame().getId());
        assertThat(player.getName()).isEqualTo(SOME_PLAYER_ENTITY.getName());
        assertThat(player.getCards()).containsExactly(SOME_CARD);
    }
}
