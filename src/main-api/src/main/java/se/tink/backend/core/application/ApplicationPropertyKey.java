package se.tink.backend.core.application;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Objects;

public enum ApplicationPropertyKey {

    EXTERNAL_STATUS("external-status"),
    EXTERNAL_STATUS_DESCRIPTION("external-status-description"),
    EXTERNAL_APPLICATION_ID("external-application-id"),
    PRODUCT_INSTANCE_ID("product-instance-id"),
    PRODUCT_PROVIDER_NAME("product-provider-name"),
    PROPERTY_ID("property-id");

    private final String key;

    ApplicationPropertyKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    @JsonCreator
    public static ApplicationPropertyKey fromKey(String key) {
        for (ApplicationPropertyKey value : values()) {
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
