package se.tink.backend.core.application;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Objects;

public enum RefreshApplicationParameterKey {
    EXTERNAL_ID("external-id");

    private final String key;

    RefreshApplicationParameterKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    @JsonCreator
    public static RefreshApplicationParameterKey fromKey(String key) {
        for (RefreshApplicationParameterKey value : values()) {
            if (Objects.equal(value.getKey(), key)) {
                return value;
            }
        }

        throw new IllegalArgumentException(String.format("No key found: %s", key));
    }

    /**
     * Need to override the toString() for Jackson to get the key in maps from custom getter
     */
    @JsonValue
    @Override
    public String toString() {
        return getKey();
    }
}
