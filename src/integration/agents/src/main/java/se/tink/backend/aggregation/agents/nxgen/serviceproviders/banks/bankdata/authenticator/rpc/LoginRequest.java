package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginRequest {
    private String loginToken;
    private String pinCode;
    private String userId;
    private String timeToken;

    public LoginRequest setLoginToken(String loginToken) {
        this.loginToken = loginToken;
        return this;
    }

    public LoginRequest setPinCode(String pinCode) {
        this.pinCode = pinCode;
        return this;
    }

    public LoginRequest setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public LoginRequest setTimeToken(String timeToken) {
        this.timeToken = timeToken;
        return this;
    }
}
