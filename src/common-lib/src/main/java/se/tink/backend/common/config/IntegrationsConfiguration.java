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

    @JsonProperty
    private String proxyUri;

    public SbabIntegrationConfiguration getSbab() {
        return sbab;
    }

    public String getUkOpenBankingJson() {
        return ukOpenBankingJson;
    }

    public MonzoConfiguration getMonzoConfiguration() {
        return monzoConfiguration;
    }

    public String getProxyUri() {
        return proxyUri;
    }
}
