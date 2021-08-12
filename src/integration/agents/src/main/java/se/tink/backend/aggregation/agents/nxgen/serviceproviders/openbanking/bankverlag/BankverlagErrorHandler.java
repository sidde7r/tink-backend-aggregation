package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.backend.aggregation.agents.utils.berlingroup.error.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public abstract class BankverlagErrorHandler {

    enum ErrorSource {
        AUTHORISATION_USERNAME_PASSWORD
    }

    void handleError(HttpResponseException httpResponseException, ErrorSource errorSource) {
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
        return Optional.empty();
    }

    protected abstract Optional<AgentError> handleUsernamePasswordErrors(
            ErrorResponse errorResponse);
}
