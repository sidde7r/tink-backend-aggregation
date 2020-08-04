package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.authenticator;

import java.util.Optional;
import org.apache.commons.httpclient.HttpStatus;
import org.slf4j.Logger;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentExceptionImpl;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class SamlinkAuthenticatorBase {
    protected final Credentials credentials;
    protected final SamlinkApiClient apiClient;
    private final Logger logger;

    SamlinkAuthenticatorBase(Logger logger, Credentials credentials, SamlinkApiClient apiClient) {
        this.logger = logger;
        this.credentials = credentials;
        this.apiClient = apiClient;
    }

    void handleAndThrowInitError(HttpResponseException e)
            throws AuthenticationException, AuthorizationException {
        handleAndThrowAuthenticationError("loginRequest", e);
    }

    void handleAndThrowAuthenticateError(HttpResponseException e)
            throws AuthenticationException, AuthorizationException {
        handleAndThrowAuthenticationError("registerDevice", e);
    }

    void handleAndThrowAutoAuthenticateError(HttpResponseException e) throws SessionException {

        if (e.getResponse().getStatus() == HttpStatus.SC_UNAUTHORIZED) {
            ErrorResponse errorResponse = ErrorResponse.fromHttpResponseException(e);

            if (errorResponse.hasError(SamlinkConstants.ServerError.LOGIN_FAILED)) {
                throw SessionError.SESSION_EXPIRED.exception(e);
            } else {
                logger.warn(formatErrorMessage("loginRequest", errorResponse), e);
            }
        }
        throw e;
    }

    private void handleAndThrowAuthenticationError(String action, HttpResponseException e)
            throws AuthenticationException, AuthorizationException {

        ErrorResponse errorResponse = ErrorResponse.fromHttpResponseException(e);

        Optional<SamlinkConstants.ServerError> userError = errorResponse.toUserError();
        if (userError.isPresent()) {
            AgentExceptionImpl exception = userError.get().exception();
            if (exception instanceof AuthenticationException) {
                throw (AuthenticationException) exception;
            }

            if (exception instanceof AuthorizationException) {
                throw (AuthorizationException) exception;
            }
        }
        logger.warn(formatErrorMessage(action, errorResponse), e);
        throw e;
    }

    private String formatErrorMessage(String action, ErrorResponse errorResponse) {
        return String.format(
                "%s: Unknown error code for %s: %s",
                SamlinkConstants.LogTags.AUTHENTICATION,
                action,
                String.join(",", errorResponse.getErrorCodes()));
    }
}
