package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitiateSessionRequest {
    @JsonProperty("consumerID")
    private String consumerId;

    public InitiateSessionRequest(String consumerId) {
        this.consumerId = consumerId;
    }
}
