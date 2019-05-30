package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {
    private int statusCode;
    private String message;
    private String time;
    private List<Object> errors;

    @JsonIgnore
    public boolean isUserCancel() {
        String lowerCaseMessage = Optional.ofNullable(message).orElse("").toLowerCase();

        return lowerCaseMessage.contains(AvanzaConstants.AuthError.USER_CANCEL)
                || lowerCaseMessage.contains(AvanzaConstants.AuthError.CANCELLED);
    }

    @JsonIgnore
    public boolean isBankIdTimeout() {
        String lowerCaseMessage = Optional.ofNullable(message).orElse("").toLowerCase();

        return lowerCaseMessage.contains(AvanzaConstants.AuthError.TIMEOUT);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public String getTime() {
        return time;
    }

    public List<Object> getErrors() {
        return errors;
    }
}
