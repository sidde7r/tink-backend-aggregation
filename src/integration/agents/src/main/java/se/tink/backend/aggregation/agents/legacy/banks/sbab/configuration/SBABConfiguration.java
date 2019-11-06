package se.tink.backend.aggregation.agents.banks.sbab.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
public class SBABConfiguration implements ClientConfiguration {
    @JsonProperty private String signBaseUrl;

    public String getSignBaseUrl() {
        return signBaseUrl;
    }
}
