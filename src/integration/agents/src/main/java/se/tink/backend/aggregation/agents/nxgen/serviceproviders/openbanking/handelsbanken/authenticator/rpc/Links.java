package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Links {

    @JsonProperty("cancel")
    private Cancel cancel;

    @JsonProperty("token")
    private Token token;

    public Cancel getCancel() {
        return cancel;
    }

    public Token getToken() {
        return token;
    }
}
