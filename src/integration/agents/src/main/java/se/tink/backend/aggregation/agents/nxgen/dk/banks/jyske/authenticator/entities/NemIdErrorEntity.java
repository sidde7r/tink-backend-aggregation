package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities;

import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.JyskeConstants.ErrorCode;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@JsonObject
public class NemIdErrorEntity {

    private String errorCode;
    private String errorMessage;

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public static void throwError(HttpResponseException e) throws LoginException {
        NemIdErrorEntity error = e.getResponse().getBody(NemIdErrorEntity.class);
        switch (Integer.valueOf(error.getErrorCode())) {
            case ErrorCode.INVALID_CREDENTIAL:
                throw LoginError.INCORRECT_CREDENTIALS.exception(e);
            case ErrorCode.NOT_SIGNED_UP_FOR_MOBILE_BANK:
                throw LoginError.NO_ACCESS_TO_MOBILE_BANKING.exception(e);
            case ErrorCode.INROLL_BAD_REQUEST:
                throw BankServiceError.BANK_SIDE_FAILURE.exception(e);
            default:
                throw new IllegalStateException(
                        String.format(
                                "ErrorCode: %s, errorMsg: %s.",
                                error.getErrorCode(), error.getErrorMessage()),
                        e);
        }
    }
}
