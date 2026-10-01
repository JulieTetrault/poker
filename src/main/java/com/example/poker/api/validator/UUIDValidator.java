package com.example.poker.api.validator;

import com.example.poker.api.exception.InvalidIdentifierException;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UUIDValidator {
    public UUID validate(String value, String field) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new InvalidIdentifierException(field);
        }
    }
}
