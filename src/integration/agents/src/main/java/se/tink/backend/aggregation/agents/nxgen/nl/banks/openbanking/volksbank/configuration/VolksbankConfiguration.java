package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.configuration.entity.AISClientConfigurationEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class VolksbankConfiguration implements ClientConfiguration {

    @JsonProperty private String redirectUrl;
    @JsonProperty private AISClientConfigurationEntity aisConfiguration;

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public AISClientConfigurationEntity getAisConfiguration() {
        return aisConfiguration;
    }
}
