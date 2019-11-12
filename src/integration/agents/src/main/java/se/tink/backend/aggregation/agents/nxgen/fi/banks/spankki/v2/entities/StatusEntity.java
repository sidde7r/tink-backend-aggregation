package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiConstants.StatusMessages;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class StatusEntity {
    @JsonProperty private String code = "";
    @JsonProperty private String message = "";
    @JsonProperty private String localizedMessage = "";
    @JsonProperty private String sessionId = "";
    @JsonProperty private boolean isLoggedIn;
    @JsonProperty private boolean isTupasComplete;

    @JsonIgnore
    public String getSessionId() {
        return sessionId;
    }

    @JsonIgnore
    public String getMessage() {
        return message;
    }

    @JsonIgnore
    public String getLocalizedMessage() {
        return localizedMessage;
    }

    @JsonIgnore
    public String getStatusCode() {
        return code;
    }

    @JsonIgnore
    public boolean isBankSideFailure() {
        return code.equals(StatusMessages.INTERNAL_ERROR_CODE)
                || message.equalsIgnoreCase(StatusMessages.INTERNAL_ERROR_MESSAGE);
    }

    @JsonIgnore
    public boolean isSessionExpired() {
        return !isLoggedIn || message.equalsIgnoreCase(StatusMessages.SESSION_EXPIRED_MESSAGE);
    }

    @JsonIgnore
    public boolean isUserBlocked() {
        return message.equalsIgnoreCase(StatusMessages.USER_LOCKED);
    }
}
