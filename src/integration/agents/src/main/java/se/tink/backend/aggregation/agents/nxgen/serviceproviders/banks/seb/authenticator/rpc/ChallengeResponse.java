package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ChallengeResponse {
    @JsonProperty("challenge_type")
    private String challengeType;

    private String d1;
    private String d2;

    public String getD1() {
        return d1;
    }

    public String getD2() {
        return d2;
    }
}
