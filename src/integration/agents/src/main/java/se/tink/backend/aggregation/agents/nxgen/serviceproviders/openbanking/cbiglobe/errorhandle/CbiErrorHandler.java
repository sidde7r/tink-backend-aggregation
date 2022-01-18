package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.errorhandle;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.RequestContext;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Slf4j
public class CbiErrorHandler {

    public static void handleError(
            HttpResponseException httpResponseException, RequestContext context) {
        if (!httpResponseException.getResponse().hasBody()) {
            return;
        }

        if (isInternalBankError(httpResponseException)) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(httpResponseException.getCause());
        }

        handleKnownError(httpResponseException, context);
    }

    private static void handleKnownError(
            HttpResponseException httpResponseException, RequestContext context) {

        ErrorResponse errorResponse = ErrorResponse.createFrom(httpResponseException.getResponse());

        if (errorResponse == null) {
            return;
        }

        log.error(String.format("Handling error in context: %s", context.name()));

        if (RequestContext.PSU_CREDENTIALS_UPDATE.equals(context)) {
            throwIfMatches(
                    errorResponse,
                    ErrorMessages.PSU_CREDENTIALS_INVALID,
                    LoginError.INCORRECT_CREDENTIALS.exception(httpResponseException));
        }

        throwIfMatches(
                errorResponse,
                ErrorMessages.RESOURCE_UNKNOWN,
                BankServiceError.BANK_SIDE_FAILURE.exception(httpResponseException));

        throwIfMatches(
                errorResponse,
                ErrorMessages.PSU_CREDENTIALS_INVALID,
                LoginError.INCORRECT_CHALLENGE_RESPONSE.exception(httpResponseException));
    }

    private static void throwIfMatches(
            ErrorResponse errorResponse, String expectedErrorText, RuntimeException exception) {
        boolean found =
                errorResponse.getTppMessages().stream()
                        .filter(ErrorTppMessage::isError)
                        .anyMatch(
                                x ->
                                        (expectedErrorText.equalsIgnoreCase(x.getCode())
                                                || expectedErrorText.equalsIgnoreCase(
                                                        x.getText())));
        if (found) {
            throw exception;
        }
    }

    private static boolean isInternalBankError(HttpResponseException httpResponseException) {
        int statusCode = httpResponseException.getResponse().getStatus();
        return statusCode >= 500 && statusCode <= 511;
    }
}
