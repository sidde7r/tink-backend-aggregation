package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.authenticator.rpc.InternalErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.authenticator.rpc.MinimumViableAuthenticationInitResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.authenticator.rpc.RemainingAttemptResponse;
import se.tink.backend.aggregation.nxgen.http.DefaultResponseStatusHandler;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class NickelResponseHandler extends DefaultResponseStatusHandler {

    public static final String EXPIRED_CHALLENGE = "error.expiredChallenge";
    public static final String TOO_MANY_CHALLENGES = "error.tooManyChallenges";
    public static final String TO_MANY_ATTEMPTS = "error.tooManyPasswordAttempts";
    public static final String CONTACT_WITH_BANK =
            "User account is blocked. Please contact your bank.";

    @Override
    public void handleResponse(HttpRequest httpRequest, HttpResponse httpResponse) {
        switch (httpResponse.getStatus()) {
            case HttpStatus.SC_BAD_REQUEST:
                throw badRequest(httpResponse);
            case HttpStatus.SC_UNAUTHORIZED:
                if (!hasBodyofClass(httpResponse, MinimumViableAuthenticationInitResponse.class)) {
                    handleUnauthorized(httpResponse);
                }
                break;
            case HttpStatus.SC_FORBIDDEN:
                throw handleForbidden(httpResponse);
            case HttpStatus.SC_INTERNAL_SERVER_ERROR:
                throw handleInternalServerError(httpResponse);
            default:
                break;
        }
        super.handleResponse(httpRequest, httpResponse);
    }

    private RuntimeException handleForbidden(HttpResponse httpResponse) {
        if (hasBodyofClass(httpResponse, ErrorResponse.class)) {
            ErrorResponse response = httpResponse.getBody(ErrorResponse.class);
            if (TO_MANY_ATTEMPTS.equals(response.getMessage())) {
                throw AuthorizationError.ACCOUNT_BLOCKED.exception(response.getMessage());
            } else {
                throw AuthorizationError.ACCOUNT_BLOCKED.exception(CONTACT_WITH_BANK);
            }
        }
        throw SessionError.SESSION_EXPIRED.exception(httpResponse.getBody(String.class));
    }

    private RuntimeException badRequest(HttpResponse httpResponse) {
        if (hasBodyofClass(httpResponse, ErrorResponse.class)) {
            ErrorResponse response = httpResponse.getBody(ErrorResponse.class);
            if (EXPIRED_CHALLENGE.equals(response.getMessage())) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            } else if (TOO_MANY_CHALLENGES.equals(response.getMessage())) {
                throw AuthorizationError.ACCOUNT_BLOCKED.exception(response.getMessage());
            }
        }
        throw SessionError.SESSION_EXPIRED.exception(httpResponse.getBody(String.class));
    }

    private void handleUnauthorized(HttpResponse httpResponse) {

        if (hasBodyofClass(httpResponse, RemainingAttemptResponse.class)) {
            RemainingAttemptResponse response =
                    httpResponse.getBody(RemainingAttemptResponse.class);
            switch (response.getRemainingAttemptCount()) {
                case 0:
                    throw AuthorizationError.ACCOUNT_BLOCKED.exception();
                case 1:
                    throw LoginError.INCORRECT_CREDENTIALS_LAST_ATTEMPT.exception();
                default:
                    throw LoginError.INCORRECT_CREDENTIALS.exception();
            }
        }
        if (hasBodyofClass(httpResponse, ErrorResponse.class)) {
            throw AuthorizationError.UNAUTHORIZED.exception(
                    httpResponse.getBody(ErrorResponse.class).getMessage());
        } else {
            throw AuthorizationError.UNAUTHORIZED.exception(httpResponse.getBody(String.class));
        }
    }

    private RuntimeException handleInternalServerError(HttpResponse httpResponse) {
        if (hasBodyofClass(httpResponse, InternalErrorResponse.class)) {
            return BankServiceError.BANK_SIDE_FAILURE.exception(
                    httpResponse.getBody(InternalErrorResponse.class).getDetail());
        } else {
            return BankServiceError.BANK_SIDE_FAILURE.exception(httpResponse.getBody(String.class));
        }
    }

    private <T> boolean hasBodyofClass(HttpResponse response, Class<T> cls) {
        try {
            if (!response.hasBody()) {
                return false;
            }
            ObjectMapper mapper = new ObjectMapper();
            mapper.readValue(response.getBody(String.class), cls);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }
}
