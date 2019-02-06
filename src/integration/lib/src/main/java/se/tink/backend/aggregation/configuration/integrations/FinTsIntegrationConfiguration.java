package se.tink.backend.aggregation.configuration.integrations;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FinTsIntegrationConfiguration {

    @JsonProperty
    private String regNumber;

    public String getRegNumber() {
        return regNumber;
    }
}
