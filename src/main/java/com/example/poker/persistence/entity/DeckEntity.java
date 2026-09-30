package com.example.poker.persistence.entity;

import com.example.poker.persistence.converter.CardListConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "decks")
public class DeckEntity {

    @Id private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    private GameEntity game;

    @Lob
    @Column(name = "cards", nullable = false)
    @Convert(converter = CardListConverter.class)
    private List<CardEntity> cards = new ArrayList<>();

    protected DeckEntity() {}

    public DeckEntity(UUID id, GameEntity game, List<CardEntity> cards) {
        this.id = id;
        this.game = game;
        this.cards = new ArrayList<>(cards);
    }

    public UUID getId() {
        return id;
    }

    public GameEntity getGame() {
        return game;
    }

    public List<CardEntity> getCards() {
        return cards;
    }
}
