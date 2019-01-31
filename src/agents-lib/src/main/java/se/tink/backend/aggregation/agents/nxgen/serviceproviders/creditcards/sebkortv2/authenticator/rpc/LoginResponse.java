package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginResponse {
    private String action;
    private String target;
    private String uid;
    private String type;

    @JsonProperty("seb_Auth_Mechanism")
    private String sebAuthMechanism;

    @JsonProperty("seb_Referer")
    private String sebReferer;

    @JsonProperty("scrt")
    private String secret;

    public String getSecret() {
        return secret;
    }

    public String getUid() {
        return uid;
    }
}
