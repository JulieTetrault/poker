package com.example.poker.persistence.entity;

import com.example.poker.domain.model.Rank;
import com.example.poker.domain.model.Suit;
import java.io.Serializable;

public record CardEntity(Suit suit, Rank rank) implements Serializable {}
