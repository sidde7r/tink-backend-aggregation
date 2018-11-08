package se.tink.backend.aggregation.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SbabIntegrationConfiguration {
    @JsonProperty
    private String signBaseUrl;

    public String getSignBaseUrl() {
        return signBaseUrl;
    }
}
