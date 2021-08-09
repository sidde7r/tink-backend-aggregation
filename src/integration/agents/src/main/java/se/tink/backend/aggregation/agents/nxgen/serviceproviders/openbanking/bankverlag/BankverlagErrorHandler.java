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
        Optional<ErrorResponse> maybeErrorResponse =
                ErrorResponse.fromHttpException(httpResponseException);

        if (maybeErrorResponse.isPresent()) {
            ErrorResponse errorResponse = maybeErrorResponse.get();
            AgentError error = null;

            if (errorSource == ErrorSource.AUTHORISATION_USERNAME_PASSWORD) {
                error = handleUsernamePasswordErrors(errorResponse);
            }

            if (error != null) {
                throw error.exception(httpResponseException);
            }
        }
    }

    protected abstract AgentError handleUsernamePasswordErrors(ErrorResponse errorResponse);
}
