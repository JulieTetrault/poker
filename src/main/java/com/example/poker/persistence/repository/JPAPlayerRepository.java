package com.example.poker.persistence.repository;

import com.example.poker.persistence.entity.PlayerEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JPAPlayerRepository extends JpaRepository<PlayerEntity, UUID> {}
