package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.utils.berlingroup.error.ErrorResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.error.TppMessage;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public abstract class BankverlagErrorHandler {

    private static final TppMessage PSU_CREDENTIALS_INVALID =
            TppMessage.builder().category(TppMessage.ERROR).code("PSU_CREDENTIALS_INVALID").build();

    public enum ErrorSource {
        AUTHORISATION_USERNAME_PASSWORD,
        OTP_STEP
    }

    public void handleError(HttpResponseException httpResponseException, ErrorSource errorSource) {
        ErrorResponse.fromHttpException(httpResponseException)
                .flatMap(errorResponse -> findErrorForResponse(errorResponse, errorSource))
                .ifPresent(
                        x -> {
                            throw x.exception(httpResponseException);
                        });
    }

    private Optional<AgentError> findErrorForResponse(
            ErrorResponse errorResponse, ErrorSource errorSource) {
        if (errorSource == ErrorSource.AUTHORISATION_USERNAME_PASSWORD) {
            return handleUsernamePasswordErrors(errorResponse);
        }
        if (errorSource == ErrorSource.OTP_STEP) {
            return handleOtpErrors(errorResponse);
        }
        return Optional.empty();
    }

    protected Optional<AgentError> handleUsernamePasswordErrors(ErrorResponse errorResponse) {
        if (ErrorResponse.anyTppMessageMatchesPredicate(PSU_CREDENTIALS_INVALID)
                .test(errorResponse)) {
            return Optional.of(LoginError.INCORRECT_CREDENTIALS);
        }
        return Optional.empty();
    }

    protected Optional<AgentError> handleOtpErrors(ErrorResponse errorResponse) {
        if (ErrorResponse.anyTppMessageMatchesPredicate(PSU_CREDENTIALS_INVALID)
                .test(errorResponse)) {
            return Optional.of(LoginError.INCORRECT_CHALLENGE_RESPONSE);
        }
        return Optional.empty();
    }
}
