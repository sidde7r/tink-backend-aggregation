package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {
    private ErrorMessagesEntity errorMessages;

    @JsonIgnore
    public boolean hasErrorCode(String errorCode) {
        if (errorMessages != null && errorMessages.getGeneral() != null) {
            return errorMessages.getGeneral().stream()
                    .anyMatch(generalEntity -> generalEntity.getCode().equalsIgnoreCase(errorCode));
        }

        return false;
    }

    @JsonIgnore
    public String getAllErrors() {
        if (errorMessages == null || errorMessages.getGeneral() == null) {
            return "";
        }

        return errorMessages.getGeneral().stream()
                .map(generalEntity -> String.format("%s: %s", generalEntity.getCode(), generalEntity.getMessage()))
                .collect(Collectors.joining("\n"));
    }

    public ErrorMessagesEntity getErrorMessages() {
        return errorMessages;
    }
}
