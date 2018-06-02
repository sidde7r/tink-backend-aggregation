package se.tink.backend.common.providers.booli.entities.request;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SewerType implements BooliParameterType {
    MUNICIPAL("municipal"), INDIVIDUAL("individual"), NONE("none");

    private final String value;

    SewerType(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String value() {
        return value;
    }
}
