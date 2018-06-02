package se.tink.backend.common.providers.booli.entities.request;

import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Strings;
import se.tink.libraries.application.ApplicationFieldOptionValues;

public enum BalconyType implements BooliParameterType {
    TRADITIONAL("traditional"), FRENCH("french"), DOUBLE_TRADITIONAL("double-traditional"),
    TERRACE("terrace"), ROYAL_BALCONY("royal-balcony"), PATIO("patio"), NONE("none");

    private final String value;

    BalconyType(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String value() {
        return value;
    }

    public static BalconyType fromApplicationOptionValue(String value) {
        if (Strings.isNullOrEmpty(value)) {
            return null;
        }

        switch (value) {
        case ApplicationFieldOptionValues.BALCONY_TRADITIONAL:
            return TRADITIONAL;
        case ApplicationFieldOptionValues.BALCONY_FRENCH:
            return FRENCH;
        case ApplicationFieldOptionValues.BALCONY_DOUBLE_TRADITIONAL:
            return DOUBLE_TRADITIONAL;
        case ApplicationFieldOptionValues.BALCONY_TERRACE:
            return TERRACE;
        case ApplicationFieldOptionValues.BALCONY_ROYAL:
            return ROYAL_BALCONY;
        case ApplicationFieldOptionValues.BALCONY_PATIO:
            return PATIO;
        case ApplicationFieldOptionValues.NO:
            return NONE;
        default:
            return null;
        }
    }
}
