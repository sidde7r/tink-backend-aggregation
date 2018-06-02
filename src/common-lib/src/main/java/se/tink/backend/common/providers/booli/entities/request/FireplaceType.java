package se.tink.backend.common.providers.booli.entities.request;

import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Strings;
import se.tink.libraries.application.ApplicationFieldOptionValues;

public enum FireplaceType implements BooliParameterType {
    FUNCTIONAL("functional"), NON_FUNCTIONAL("non-functional"), NONE("no");

    private final String value;

    FireplaceType(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String value() {
        return value;
    }

    public static FireplaceType fromApplicationOptionValue(String value) {
        if (Strings.isNullOrEmpty(value)) {
            return null;
        }

        switch (value) {
        case ApplicationFieldOptionValues.FIREPLACE_FUNCTIONAL:
            return FUNCTIONAL;
        case ApplicationFieldOptionValues.FIREPLACE_NON_FUNCTIONAL:
            return NON_FUNCTIONAL;
        case ApplicationFieldOptionValues.NO:
            return NONE;
        default:
            return null;
        }
    }
}
