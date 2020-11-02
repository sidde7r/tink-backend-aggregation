package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthDeviceResponse {
    private String token;

    @JsonProperty("user_id")
    private String userId;

    public String getToken() {
        return token;
    }
}
