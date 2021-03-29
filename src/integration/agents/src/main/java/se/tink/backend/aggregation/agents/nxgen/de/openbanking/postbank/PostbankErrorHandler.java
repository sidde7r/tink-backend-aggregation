package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank;

import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.rpc.transactions.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.rpc.transactions.ErrorTppMessage;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class PostbankErrorHandler {

    private static final String ERR_BAD_REQUEST = "Bad Request";
    private static final String ERR_CREDENTIALS_INVALID = "PSU_CREDENTIALS_INVALID";
    private static final String ERR_SCA_METHOD_UNKNOWN = "SCA_METHOD_UNKNOWN";

    enum ErrorSource {
        CONSENT_CREATION,
        AUTHORISATION_PASSWORD,
        AUTHORISATION_OTP,
        AUTHORISATION_UPDATE,
        AUTHORISATION_FETCH
    }

    public static void handleError(
            HttpResponseException httpResponseException, ErrorSource errorSource) {
        if (!httpResponseException.getResponse().hasBody()) {
            return;
        }

        if (isInternalBankError(httpResponseException)) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(httpResponseException.getCause());
        }

        handleKnownError(httpResponseException, errorSource);
    }

    private static void handleKnownError(
            HttpResponseException httpResponseException, ErrorSource errorSource) {

        ErrorResponse errorResponse = getBodyAsExpectedType(httpResponseException.getResponse());
        if (errorResponse == null) {
            return;
        }

        switch (errorSource) {
            case CONSENT_CREATION:
                throwIfMatches(
                        errorResponse,
                        ERR_BAD_REQUEST,
                        LoginError.INCORRECT_CREDENTIALS.exception(httpResponseException));
                throwIfMatches(
                        errorResponse,
                        ERR_SCA_METHOD_UNKNOWN,
                        LoginError.NO_AVAILABLE_SCA_METHODS.exception(httpResponseException));
                break;
            case AUTHORISATION_PASSWORD:
                throwIfMatches(
                        errorResponse,
                        ERR_CREDENTIALS_INVALID,
                        LoginError.INCORRECT_CREDENTIALS.exception(httpResponseException));
                break;
            case AUTHORISATION_OTP:
                throwIfMatches(
                        errorResponse,
                        ERR_CREDENTIALS_INVALID,
                        LoginError.INCORRECT_CHALLENGE_RESPONSE.exception(httpResponseException));
                break;
            default:
                break;
        }
    }

    private static void throwIfMatches(
            ErrorResponse errorResponse, String expectedErrorCode, RuntimeException toThrow) {
        boolean found =
                errorResponse.getTppMessages().stream()
                        .filter(ErrorTppMessage::isError)
                        .anyMatch(x -> expectedErrorCode.equalsIgnoreCase(x.getCode()));
        if (found) {
            throw toThrow;
        }
    }

    private static ErrorResponse getBodyAsExpectedType(HttpResponse response) {
        try {
            return response.getBody(ErrorResponse.class);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static boolean isInternalBankError(HttpResponseException httpResponseException) {
        int statusCode = httpResponseException.getResponse().getStatus();
        return statusCode >= 500 && statusCode <= 511;
    }
}
