package com.example.poker.domain.factory;

import java.util.UUID;

@FunctionalInterface
public interface IdGenerator {
    UUID nextId();
}
