package com.example.poker.persistence.entity;

import com.example.poker.persistence.converter.CardListConverter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "games")
public class GameEntity {
    @Id private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL)
    private List<DeckEntity> decks = new ArrayList<>();

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlayerEntity> players = new ArrayList<>();

    @Lob
    @Column(name = "undealt_cards", nullable = false)
    @Convert(converter = CardListConverter.class)
    private List<CardEntity> undealtCards = new ArrayList<>();

    protected GameEntity() {}

    public GameEntity(UUID id, String name) {
        this(id, name, List.of());
    }

    public GameEntity(UUID id, String name, List<CardEntity> undealtCards) {
        this.id = id;
        this.name = name;
        this.undealtCards = new ArrayList<>(undealtCards);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<DeckEntity> getDecks() {
        return decks;
    }

    public List<PlayerEntity> getPlayers() {
        return players;
    }

    public void addDeck(DeckEntity deck) {
        decks.add(deck);
    }

    public void addPlayer(PlayerEntity player) {
        players.add(player);
    }

    public List<CardEntity> getUndealtCards() {
        return undealtCards;
    }
}
