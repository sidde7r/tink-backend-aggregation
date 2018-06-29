package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {
    private ErrorMessagesEntity errorMessages;

    @JsonIgnore
    public boolean hasErrorCode(String anErrorCode) {
        if (errorMessages != null && errorMessages.getGeneral() != null) {
            return errorMessages.getGeneral().stream()
                    .anyMatch(generalEntity -> generalEntity.getCode().equalsIgnoreCase(anErrorCode));
        }

        return false;
    }

    public ErrorMessagesEntity getErrorMessages() {
        return errorMessages;
    }
}
