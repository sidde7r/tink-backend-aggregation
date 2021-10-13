package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.stream.Collectors;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ErrorResponse {
    private ErrorMessagesEntity errorMessages;

    public boolean isLoginFailedError() {
        return hasErrorCode(SwedbankBaseConstants.BankErrorMessage.LOGIN_FAILED);
    }

    public boolean isSessionInvalidatedError() {
        return hasErrorCode(SwedbankBaseConstants.BankErrorMessage.SESSION_INVALIDATED);
    }

    @JsonIgnore
    public boolean hasErrorCode(String errorCode) {
        if (isErrorMsgAndErrorMsgGeneralNotNull()) {
            return isErrorMsgGeneralAndGivenErrorCodeSame(errorCode);
        }

        return false;
    }

    private boolean isErrorMsgGeneralAndGivenErrorCodeSame(String errorCode) {
        return errorMessages.getGeneral().stream()
                .anyMatch(
                        errorDetailsEntity ->
                                errorDetailsEntity.getCode().equalsIgnoreCase(errorCode));
    }

    @JsonIgnore
    public boolean hasErrorField(String errorField) {
        if (isErrorMsgAndErrorMsgFieldsNotNull()) {
            return errorMessages.getFields().stream()
                    .anyMatch(fieldEntity -> fieldEntity.getField().equalsIgnoreCase(errorField));
        }

        return false;
    }

    @JsonIgnore
    public boolean hasErrorMessage(String errorMessage) {
        if (isErrorMsgAndErrorMsgFieldsNotNull()) {
            return errorMessages.getFields().stream()
                    .anyMatch(
                            fieldEntity -> fieldEntity.getMessage().equalsIgnoreCase(errorMessage));
        }

        return false;
    }

    private boolean isErrorMsgAndErrorMsgFieldsNotNull() {
        return errorMessages != null && errorMessages.getFields() != null;
    }

    @JsonIgnore
    public String getAllErrors() {

        String msg = "";
        if (isErrorMsgAndErrorMsgGeneralNotNull()) {
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

        if (isErrorMsgAndErrorMsgFieldsNotNull()) {
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

    private boolean isErrorMsgAndErrorMsgGeneralNotNull() {
        return errorMessages != null && errorMessages.getGeneral() != null;
    }
}
