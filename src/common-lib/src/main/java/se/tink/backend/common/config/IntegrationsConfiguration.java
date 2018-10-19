package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown =  true)
public class IntegrationsConfiguration {
    @JsonProperty
    private SbabIntegrationConfiguration sbab;

    @JsonProperty
    private String ukOpenBankingJson;

    @JsonProperty
    private MonzoConfiguration monzoConfiguration;

    public SbabIntegrationConfiguration getSbab() {
        return sbab;
    }

    public String getUkOpenBankingJson() {
        return ukOpenBankingJson;
    }

    public MonzoConfiguration getMonzoConfiguration() {
        return monzoConfiguration;
    }
}
