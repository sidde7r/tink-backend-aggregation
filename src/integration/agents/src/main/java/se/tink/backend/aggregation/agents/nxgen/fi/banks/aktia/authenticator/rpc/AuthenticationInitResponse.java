package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthenticationInitResponse extends TokenResponse {
    // This is populated from http headers
    @JsonIgnore private String loginStatus;

    public String getLoginStatus() {
        return loginStatus;
    }

    public void setLoginStatus(String loginStatus) {
        this.loginStatus = loginStatus;
    }
}
