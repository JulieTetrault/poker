package com.example.poker.persistence.converter;

import com.example.poker.persistence.entity.CardEntity;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.ArrayList;
import java.util.List;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@Converter
public class CardListConverter implements AttributeConverter<List<CardEntity>, String> {
    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final TypeReference<List<CardEntity>> CARD_LIST = new TypeReference<>() {};

    @Override
    public String convertToDatabaseColumn(List<CardEntity> cards) {
        return cards == null ? null : JSON.writeValueAsString(cards);
    }

    @Override
    public List<CardEntity> convertToEntityAttribute(String json) {
        return json == null ? null : new ArrayList<>(JSON.readValue(json, CARD_LIST));
    }
}
