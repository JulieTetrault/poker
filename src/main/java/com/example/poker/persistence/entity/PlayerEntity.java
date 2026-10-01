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
@Table(name = "players")
public class PlayerEntity {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private GameEntity game;

    @Column(nullable = false, length = 100)
    private String name;

    @Lob
    @Column(name = "cards", nullable = false)
    @Convert(converter = CardListConverter.class)
    private List<CardEntity> cards = new ArrayList<>();

    protected PlayerEntity() {}

    public PlayerEntity(UUID id, GameEntity game, String name, List<CardEntity> cards) {
        this.id = id;
        this.game = game;
        this.name = name;
        this.cards = new ArrayList<>(cards);
    }

    public UUID getId() {
        return id;
    }

    public GameEntity getGame() {
        return game;
    }

    public String getName() {
        return name;
    }

    public List<CardEntity> getCards() {
        return cards;
    }
}
