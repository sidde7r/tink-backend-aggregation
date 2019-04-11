package se.tink.backend.aggregation.rpc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Objects;

public enum FetchProductInformationParameterKey {
    NAME("name"),
    SSN("ssn"),
    MORTGAGE_AMOUNT("mortgage-amount"),
    DOCUMENT_IDENTIFIER("document-identifier"),
    PROPERTY_TYPE("property-type"),
    MARKET_VALUE("market-value"),
    MUNICIPALITY("municipality"),
    NEW_PLACEMENT_VOLUME("new-placement-volume"),
    NUMBER_OF_APPLICANTS("number-of-applicants");

    private final String key;

    FetchProductInformationParameterKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    @JsonCreator
    public static FetchProductInformationParameterKey fromKey(String key) {
        for (FetchProductInformationParameterKey value : values()) {
            if (Objects.equal(value.getKey(), key)) {
                return value;
            }
        }

        throw new IllegalArgumentException(String.format("No key found: %s", key));
    }

    /** Need to override the toString() for Jackson to get the key in maps from custom getter */
    @JsonValue
    @Override
    public String toString() {
        return getKey();
    }
}
