package se.tink.backend.common.providers.booli.entities.request;

import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Strings;
import se.tink.libraries.application.ApplicationFieldOptionValues;

public enum VistaType implements BooliParameterType {
    OCEAN("ocean"), SEA("sea"), ROOFTOPS("rooftops"), COURTYARD("courtyard"), NONE("none");

    private final String value;

    VistaType(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String value() {
        return value;
    }

    public static VistaType fromApplicationOptionValue(String value) {
        if (Strings.isNullOrEmpty(value)) {
            return null;
        }

        switch (value) {
        case ApplicationFieldOptionValues.VISTA_SEA:
            return OCEAN;
        case ApplicationFieldOptionValues.VISTA_LAKE:
            return SEA;
        case ApplicationFieldOptionValues.VISTA_ROOFTOPS:
            return ROOFTOPS;
        case ApplicationFieldOptionValues.VISTA_COURTYARD:
            return COURTYARD;
        case ApplicationFieldOptionValues.VISTA_NOTHING:
            return NONE;
        default:
            return null;
        }
    }
}
