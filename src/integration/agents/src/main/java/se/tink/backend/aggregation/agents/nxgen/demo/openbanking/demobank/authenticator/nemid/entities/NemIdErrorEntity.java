package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.nemid.entities;

import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@JsonObject
public class NemIdErrorEntity {

    private static final class ErrorCode {
        public static final int INVALID_CREDENTIAL = 112;
        public static final int NOT_SIGNED_UP_FOR_MOBILE_BANK = 109;
        public static final int INROLL_BAD_REQUEST = 1;
    }

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
