package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.configuration.entity.AISClientConfigurationEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.OpenBankingClientConfiguration;

@JsonObject
public class VolksbankConfiguration extends OpenBankingClientConfiguration {

    @JsonProperty private AISClientConfigurationEntity aisConfiguration;

    public AISClientConfigurationEntity getAisConfiguration() {
        return aisConfiguration;
    }
}
