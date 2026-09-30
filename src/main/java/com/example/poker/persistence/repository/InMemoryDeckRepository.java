package com.example.poker.persistence.repository;

import com.example.poker.domain.exception.NotFoundException;
import com.example.poker.domain.model.Deck;
import com.example.poker.domain.repository.DeckRepository;
import com.example.poker.persistence.entity.DeckEntity;
import com.example.poker.persistence.entity.GameEntity;
import com.example.poker.persistence.mapper.DeckEntityMapper;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class InMemoryDeckRepository implements DeckRepository {
    private final JPADeckRepository deckRepository;
    private final DeckEntityMapper deckEntityMapper;
    private final EntityManager entityManager;

    public InMemoryDeckRepository(
            JPADeckRepository deckRepository,
            DeckEntityMapper deckEntityMapper,
            EntityManager entityManager) {
        this.deckRepository = deckRepository;
        this.deckEntityMapper = deckEntityMapper;
        this.entityManager = entityManager;
    }

    @Override
    public Deck create(Deck deck) {
        return deckEntityMapper.fromEntity(deckRepository.save(deckEntityMapper.toEntity(deck)));
    }

    @Override
    public Deck update(Deck deck) {
        this.getEntityById(deck.getId());
        return deckEntityMapper.fromEntity(
                deckRepository.save(deckEntityMapper.toEntity(deck, this.resolveGameEntity(deck))));
    }

    private DeckEntity getEntityById(UUID id) {
        return deckRepository.findById(id).orElseThrow(() -> new NotFoundException("Deck", id));
    }

    private GameEntity resolveGameEntity(Deck deck) {
        return deck.getGameId() != null
                ? entityManager.getReference(GameEntity.class, deck.getGameId())
                : null;
    }
}
