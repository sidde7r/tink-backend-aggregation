package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

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
                    .anyMatch(
                            errorDetailsEntity ->
                                    errorDetailsEntity.getCode().equalsIgnoreCase(errorCode));
        }

        return false;
    }

    @JsonIgnore
    public boolean hasErrorField(String errorField) {
        if (errorMessages != null && errorMessages.getFields() != null) {
            return errorMessages.getFields().stream()
                    .anyMatch(fieldEntity -> fieldEntity.getField().equalsIgnoreCase(errorField));
        }

        return false;
    }

    @JsonIgnore
    public String getAllErrors() {

        String msg = "";
        if (errorMessages != null && errorMessages.getGeneral() != null) {
            msg +=
                    errorMessages.getGeneral().stream()
                            .map(
                                    errorDetailsEntity ->
                                            String.format(
                                                    "%s: %s",
                                                    errorDetailsEntity.getCode(),
                                                    errorDetailsEntity.getMessage()))
                            .collect(Collectors.joining("\n"));
        }

        if (errorMessages != null && errorMessages.getFields() != null) {
            msg +=
                    errorMessages.getFields().stream()
                            .map(
                                    fieldEntity ->
                                            String.format(
                                                    "%s: %s",
                                                    fieldEntity.getField(),
                                                    fieldEntity.getMessage()))
                            .collect(Collectors.joining("\n"));
        }

        return msg;
    }

    public ErrorMessagesEntity getErrorMessages() {
        return errorMessages;
    }
}
