package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.entities.ErrorMessage;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {
    @JsonProperty("message")
    private List<ErrorMessage> messages;

    public List<ErrorMessage> getMessages() {
        return messages;
    }

    @JsonIgnore
    public boolean hasErrorCode(String errorCode) {
        return hasError(ErrorMessage::getErrorCode, errorCode);
    }

    @JsonIgnore
    public boolean hasErrorField(String field) {
        return hasError(ErrorMessage::getField, field);
    }

    @JsonIgnore
    public boolean hasErrorMessage(String message) {
        return hasError(ErrorMessage::getMessage, message);
    }

    @JsonIgnore
    private boolean hasError(Function<ErrorMessage, String> mapper, String value) {
        if (Objects.isNull(messages) || messages.size() == 0) {
            return false;
        }
        return messages.stream()
                .filter(msg -> mapper.apply(msg).equalsIgnoreCase(value))
                .findFirst()
                .isPresent();
    }
}
