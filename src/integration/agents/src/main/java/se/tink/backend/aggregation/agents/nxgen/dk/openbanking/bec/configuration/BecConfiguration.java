package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.bec.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class BecConfiguration implements ClientConfiguration {
    @JsonProperty private String clientId;

    public String getClientId() {
        return clientId;
    }
}
