package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginResponse {
    private String action;
    private String target;
    private String scrt;
    private String uid;
    private String type;

    @JsonProperty("seb_Auth_Mechanism")
    private String sebAuthMechanism;

    @JsonProperty("seb_Referer")
    private String sebReferer;

    public String getScrt() {
        return scrt;
    }

    public String getUid() {
        return uid;
    }
}
