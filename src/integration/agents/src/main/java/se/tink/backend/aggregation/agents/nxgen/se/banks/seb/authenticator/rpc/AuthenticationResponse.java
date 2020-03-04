package se.tink.backend.aggregation.agents.nxgen.se.banks.seb.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SebConstants.HeaderKeys;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

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

    public String getStatus() {
        return status;
    }

    public String getHintCode() {
        return hintCode;
    }

    public static AuthenticationResponse fromHttpResponse(HttpResponse response) {
        AuthenticationResponse authenticationResponse =
                response.getBody(AuthenticationResponse.class);
        authenticationResponse.csrfToken = response.getHeaders().getFirst(HeaderKeys.X_SEB_CSRF);
        return authenticationResponse;
    }
}
