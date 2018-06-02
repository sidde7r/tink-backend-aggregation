package se.tink.backend.common.providers.booli.entities.request;

import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Strings;
import se.tink.libraries.application.ApplicationFieldOptionValues;

public enum ResidenceType implements BooliParameterType {
    APARTMENT("Lägenhet"), HOUSE("Villa"), TERRACED("Radhus"), CHAIN_TERRACED("Kedjehus"), SEMI_DETACHED("Parhus");

    private final String value;

    ResidenceType(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String value() {
        return value;
    }

    public static ResidenceType fromApplicationOptionValue(String value) {
        if (Strings.isNullOrEmpty(value)) {
            throw new IllegalArgumentException(String.format("ResidenceType not recognized: %s", value));
        }

        switch (value) {
        case ApplicationFieldOptionValues.APARTMENT:
            return APARTMENT;
        case ApplicationFieldOptionValues.HOUSE:
        case ApplicationFieldOptionValues.VACATION_HOUSE: // We differ in the app, but they don't in the old API
            return HOUSE;
        case ApplicationFieldOptionValues.TERRACE_HOUSE:
            return TERRACED;
        case ApplicationFieldOptionValues.CHAIN_TERRACE_HOUSE:
            return CHAIN_TERRACED;
        case ApplicationFieldOptionValues.SEMI_DETACHED_HOUSE:
            return SEMI_DETACHED;
        default:
            throw new IllegalArgumentException(String.format("ResidenceType not recognized: %s", value));
        }
    }
}
