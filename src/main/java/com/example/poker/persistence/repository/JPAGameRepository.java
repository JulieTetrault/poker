package com.example.poker.persistence.repository;

import com.example.poker.persistence.entity.GameEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JPAGameRepository extends JpaRepository<GameEntity, UUID> {}
