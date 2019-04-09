package se.tink.backend.aggregation.agents.brokers.avanza.v2.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.brokers.avanza.AvanzaV2Constants;
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

        return lowerCaseMessage.contains(AvanzaV2Constants.AuthError.USER_CANCEL)
                || lowerCaseMessage.contains(AvanzaV2Constants.AuthError.CANCELLED);
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
