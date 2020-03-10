package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AuthenticationsPatchResponse {
    private String code;
    private String hsession;
    private String nemIdToken;
    private String sessionId;

    private String status;

    public String getCode() {
        return code;
    }

    public String getHsession() {
        return hsession;
    }

    public String getNemIdToken() {
        return nemIdToken;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getStatus() {
        return status;
    }
}
