package se.tink.backend.aggregation.agents.nxgen.se.banks.seb.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthenticationResponse {
    @JsonProperty("auto_start_token")
    private String autoStartToken;

    private String status;

    @JsonProperty("hint_code")
    private String hintCode;

    // Field added from header
    @JsonIgnore private String csrfToken;

    public String getAutoStartToken() {
        return autoStartToken;
    }

    public String getCsrfToken() {
        return csrfToken;
    }

    @JsonIgnore
    public AuthenticationResponse withCsrfToken(String csrfToken) {
        this.csrfToken = csrfToken;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public String getHintCode() {
        return hintCode;
    }
}
