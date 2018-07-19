package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginRequestBody {
    public String userid;
    public String pin;
    public String createSessionToken;

    public LoginRequestBody(String userid, String pin, String createSessionToken) {
        this.userid = userid;
        this.pin = pin;
        this.createSessionToken = createSessionToken;
    }
}
