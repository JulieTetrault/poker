package com.example.poker.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "decks")
public class DeckEntity {
    @Id private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    private GameEntity game;

    protected DeckEntity() {}

    public DeckEntity(UUID id, GameEntity game) {
        this.id = id;
        this.game = game;
    }

    public UUID getId() {
        return id;
    }

    public GameEntity getGame() {
        return game;
    }
}
