package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.errorhandle;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.RequestContext;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
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

        ErrorResponse errorResponse = getBodyAsExpectedType(httpResponseException.getResponse());

        if (errorResponse == null) {
            return;
        }

        log.error(String.format("Handling error in context: %s", context.name()));

        throwIfMatches(
                errorResponse,
                ErrorMessages.RESOURCE_UNKNOWN,
                BankServiceError.BANK_SIDE_FAILURE.exception(httpResponseException.getCause()));

        throwIfMatches(
                errorResponse,
                ErrorMessages.PSU_CREDENTIALS_INVALID,
                LoginError.INCORRECT_CHALLENGE_RESPONSE.exception(
                        httpResponseException.getCause()));
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
