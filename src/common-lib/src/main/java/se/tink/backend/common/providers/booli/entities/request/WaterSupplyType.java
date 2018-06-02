package se.tink.backend.common.providers.booli.entities.request;

import com.fasterxml.jackson.annotation.JsonValue;

public enum WaterSupplyType implements BooliParameterType {
    MUNICIPAL("municipal"), INDIVIDUAL("individual"), SUMMER("summer"), NONE("none");

    private final String value;

    WaterSupplyType(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String value() {
        return value;
    }
}
