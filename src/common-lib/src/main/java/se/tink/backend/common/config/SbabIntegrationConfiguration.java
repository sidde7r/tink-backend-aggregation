package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SbabIntegrationConfiguration {
    @JsonProperty
    private SbabMortgageIntegrationConfiguration mortgage;
    @JsonProperty
    private String signBaseUrl;

    public SbabMortgageIntegrationConfiguration getMortgage() {
        return mortgage;
    }

    public String getSignBaseUrl() {
        return signBaseUrl;
    }
}
