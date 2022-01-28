package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.utils.berlingroup.error.ErrorResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.error.TppMessage;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public abstract class BankverlagErrorHandler {

    private static final TppMessage PSU_CREDENTIALS_INVALID =
            TppMessage.builder().category(TppMessage.ERROR).code("PSU_CREDENTIALS_INVALID").build();

    public enum ErrorSource {
        AUTHORISATION_USERNAME_PASSWORD,
        SELECT_AUTHORIZATION_METHOD,
        OTP_STEP,
        GET_AUTHORIZATION_STATUS
    }

    public void handleError(HttpResponseException httpResponseException, ErrorSource errorSource) {
        ErrorResponse.fromHttpException(httpResponseException)
                .flatMap(
                        errorResponse ->
                                findErrorForResponse(
                                        errorResponse, errorSource, httpResponseException))
                .ifPresent(
                        exception -> {
                            throw exception;
                        });
    }

    protected Optional<RuntimeException> findErrorForResponse(
            ErrorResponse errorResponse,
            ErrorSource errorSource,
            HttpResponseException httpResponseException) {
        if (errorSource == ErrorSource.AUTHORISATION_USERNAME_PASSWORD) {
            return handleUsernamePasswordErrors(errorResponse, httpResponseException);
        }
        if (errorSource == ErrorSource.OTP_STEP) {
            return handleOtpErrors(errorResponse, httpResponseException);
        }
        return Optional.empty();
    }

    protected Optional<RuntimeException> handleUsernamePasswordErrors(
            ErrorResponse errorResponse, HttpResponseException httpResponseException) {
        if (ErrorResponse.anyTppMessageMatchesPredicate(PSU_CREDENTIALS_INVALID)
                .test(errorResponse)) {
            return Optional.of(LoginError.INCORRECT_CREDENTIALS.exception(httpResponseException));
        }
        return Optional.empty();
    }

    protected Optional<RuntimeException> handleOtpErrors(
            ErrorResponse errorResponse, HttpResponseException httpResponseException) {
        if (ErrorResponse.anyTppMessageMatchesPredicate(PSU_CREDENTIALS_INVALID)
                .test(errorResponse)) {
            return Optional.of(
                    LoginError.INCORRECT_CHALLENGE_RESPONSE.exception(httpResponseException));
        }
        return Optional.empty();
    }
}
