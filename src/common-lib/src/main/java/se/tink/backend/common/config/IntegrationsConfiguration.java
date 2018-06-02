package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IntegrationsConfiguration {
    @JsonProperty
    private SbabIntegrationConfiguration sbab;
    @JsonProperty
    private SEBIntegrationConfiguration seb;
    @JsonProperty
    private BooliIntegrationConfiguration booli;

    public SbabIntegrationConfiguration getSbab() {
        return sbab;
    }
    
    public SEBIntegrationConfiguration getSeb() {
        return seb;
    }

    public BooliIntegrationConfiguration getBooli() {
        return booli;
    }
}
