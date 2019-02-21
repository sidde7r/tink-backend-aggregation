package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class FinTsIntegrationConfiguration implements ClientConfiguration {
    @JsonProperty private String regNumber;

    public String getRegNumber() {
        return regNumber;
    }
}
