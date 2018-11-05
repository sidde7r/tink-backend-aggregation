package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LogoutResponse {
    @JsonProperty("status")
    private String status;

    public String getStatus() {
        return status;
    }
}
