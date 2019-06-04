package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginRequestBody {
    private String appid;
    public String userid;
    public String pin;
    public String createSessionToken;

    public LoginRequestBody(String appid, String userid, String pin, String createSessionToken) {
        this.appid = appid;
        this.userid = userid;
        this.pin = pin;
        this.createSessionToken = createSessionToken;
    }
}
