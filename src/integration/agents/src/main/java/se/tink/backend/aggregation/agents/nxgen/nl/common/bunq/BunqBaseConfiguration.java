package se.tink.backend.aggregation.agents.nxgen.nl.common.bunq;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BunqBaseConfiguration {

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
