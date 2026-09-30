package com.example.poker.persistence.entity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

abstract class BaseEntityTest {
    protected static EntityManagerFactory factory;

    @BeforeAll
    static void openDatabase() {
        factory = Persistence.createEntityManagerFactory("entity-tests");
    }

    @AfterAll
    static void closeDatabase() {
        if (factory != null) {
            factory.close();
        }
    }

    protected void persistEntity(Object entity) {
        try (var manager = factory.createEntityManager()) {
            manager.getTransaction().begin();
            manager.persist(entity);
            manager.getTransaction().commit();
        }
    }

    protected void withEntityManager(Consumer<EntityManager> action) {
        try (var entityManager = factory.createEntityManager()) {
            action.accept(entityManager);
        }
    }
}
