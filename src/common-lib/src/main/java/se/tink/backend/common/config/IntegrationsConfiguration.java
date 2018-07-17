package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown =  true)
public class IntegrationsConfiguration {
    @JsonProperty
    private SbabIntegrationConfiguration sbab;

    public SbabIntegrationConfiguration getSbab() {
        return sbab;
    }
    
}
