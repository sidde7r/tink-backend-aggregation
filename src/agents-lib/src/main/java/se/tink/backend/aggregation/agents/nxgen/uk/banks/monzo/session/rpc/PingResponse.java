package se.tink.backend.aggregation.agents.nxgen.uk.banks.monzo.session.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PingResponse {

    private boolean authenticated;
    @JsonProperty("client_id")
    private String clientId;
    @JsonProperty("user_id")
    private String userId;

    public boolean isAuthenticated() {
        return authenticated;
    }

}
