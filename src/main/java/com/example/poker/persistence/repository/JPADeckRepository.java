package com.example.poker.persistence.repository;

import com.example.poker.persistence.entity.DeckEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JPADeckRepository extends JpaRepository<DeckEntity, UUID> {
    void deleteByGameId(UUID gameId);
}
