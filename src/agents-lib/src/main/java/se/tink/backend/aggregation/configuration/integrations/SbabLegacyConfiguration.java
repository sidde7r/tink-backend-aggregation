package se.tink.backend.aggregation.configuration.integrations;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SbabLegacyConfiguration {
    @JsonProperty private String signBaseUrl;

    public String getSignBaseUrl() {
        return signBaseUrl;
    }
}
