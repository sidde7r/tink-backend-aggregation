package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankIdBodyEntity {
    @JsonProperty("RequestId")
    protected String requestId;
    @JsonProperty("Status")
    protected String status;
    @JsonProperty("SessionId")
    private String sessionId;
    @JsonProperty("AutostartToken")
    private String autostartToken;

    public String getRequestId() {
        return requestId;
    }

    public String getStatus() {
        return status;
    }

    public String getAutostartToken() {
        return autostartToken;
    }

    public String getSessionId() {
        return Optional.ofNullable(sessionId)
                .orElseThrow(() -> new IllegalStateException("Expected sessionId to be set but it was null"));
    }

    public boolean isTimeOut() {
        return IcaBankenConstants.BankIdStatus.TIMEOUT.equalsIgnoreCase(status);
    }
}
