package se.tink.backend.aggregation.agents.banks.sbab.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SBABConfiguration {
    @JsonProperty private String signBaseUrl;

    public String getSignBaseUrl() {
        return signBaseUrl;
    }
}
