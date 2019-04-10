package se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class SpareBank1Configuration implements ClientConfiguration {
    @JsonProperty private String clientId;
    @JsonProperty private String clientSecret;

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }
}
