package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthResponse {

    @JsonProperty("hint_code")
    private String hintCode;

    private String status;

    @JsonProperty("auto_start_token")
    private String autoStartToken;

    @JsonIgnore private String csrfToken;

    public String getHintCode() {
        return hintCode;
    }

    public String getStatus() {
        return status;
    }

    public String getAutoStartToken() {
        return autoStartToken;
    }

    public String getCsrfToken() {
        return csrfToken;
    }

    @JsonIgnore
    public AuthResponse withCsrfToken(String csrfToken) {
        this.csrfToken = csrfToken;
        return this;
    }
}
