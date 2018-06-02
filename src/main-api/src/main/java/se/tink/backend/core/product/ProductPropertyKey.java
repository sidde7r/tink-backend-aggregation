package se.tink.backend.core.product;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Objects;

public enum ProductPropertyKey {

    LIST_INTEREST_RATE("list-interest-rate"),
    INTEREST_RATE("interest-rate"),
    INTEREST_RATE_DISCOUNT("interest-rate-discount"),
    INTEREST_RATE_DISCOUNT_DESCRIPTION("interest-rate-discount-description"),
    INTEREST_RATE_DISCOUNT_DURATION_MONTHS("interest-rate-discount-duration-months"),
    AGREEMENT_URL("agreement-url"),
    VALIDITY_DURATION("validity-duration"),
    VALIDITY_END_DATE("validity-end-date");

    private final String key;

    ProductPropertyKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    @JsonCreator
    public static ProductPropertyKey fromKey(String key) {
        for (ProductPropertyKey value : values()) {
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
