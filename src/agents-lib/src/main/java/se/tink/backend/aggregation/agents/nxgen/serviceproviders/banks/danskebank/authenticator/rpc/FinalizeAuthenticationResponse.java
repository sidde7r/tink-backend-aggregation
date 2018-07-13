package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FinalizeAuthenticationResponse {
    @JsonProperty("SecurityLevel")
    private int securityLevel;
    @JsonProperty("SessionStatus")
    private int sessionStatus;
    @JsonProperty("SessionID")
    private String sessionId;
    @JsonProperty("UserID")
    private String userId;
    @JsonProperty("UserInfo")
    private UserInfoEntity userInfo;

    public int getSecurityLevel() {
        return securityLevel;
    }

    public int getSessionStatus() {
        return sessionStatus;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getUserId() {
        return userId;
    }

    public UserInfoEntity getUserInfo() {
        return userInfo;
    }
}
