package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CodeAppTokenEntity {
    @JsonProperty private String token;
    @JsonProperty private int pollTimeout;

    public String getToken() {
        return token;
    }

    public int getPollTimeout() {
        return pollTimeout;
    }
}
