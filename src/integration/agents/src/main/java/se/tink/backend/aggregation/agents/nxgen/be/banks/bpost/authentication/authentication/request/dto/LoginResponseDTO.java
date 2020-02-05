package se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.request.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginResponseDTO implements AuthenticationResponseState {

    private static final String ERROR_CODE_MOBILE_ACCESS_DELETED = "XXXX1668";

    @JsonProperty("UserSessionImpl")
    private UserSessionDTO userSession;

    private String localizedMessage;
    private String errorCode;

    public String getState() {
        return userSession.state;
    }

    public boolean isError() {
        return (userSession != null && userSession.isError) || errorCode != null;
    }

    public String getSessionId() {
        return userSession.sessionId;
    }

    public String getSessionToken() {
        return userSession.sessionToken;
    }

    public boolean isMobileAccessDeletedError() {
        return ERROR_CODE_MOBILE_ACCESS_DELETED.equals(errorCode);
    }
}
