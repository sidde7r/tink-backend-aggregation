package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
public class BunqBaseConfiguration implements ClientConfiguration {

    @JsonProperty private String backendHost;

    public BunqBaseConfiguration() {}

    BunqBaseConfiguration(String backendHost) {
        this.backendHost = backendHost;
    }

    public String getBackendHost() {
        return backendHost;
    }

    public void setBackendHost(String backendHost) {
        this.backendHost = backendHost;
    }
}
