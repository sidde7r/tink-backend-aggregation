package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.exception;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.agentplatform.authentication.result.error.NoUserInteractionResponseError;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.exception.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.exception.KnownErrorResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AccessTokenFetchingFailureError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AuthorizationError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.InvalidCredentialsError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.SessionExpiredError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ThirdPartyAppCancelledError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ThirdPartyAppNoClientError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ThirdPartyAppTimedOutError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ThirdPartyAppUnknownError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception.NemIdException;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Slf4j
public class LunarAuthenticationExceptionHandler {

    private static final Pattern INCORRECT_PASSWORD_PATTERN =
            Pattern.compile("You have [2345] attempts left\\.");
    private static final Pattern LAST_ATTEMPT_PATTERN =
            Pattern.compile("You have 1 (attempt|attempts) left\\.");

    private static final String INCORRECT_PASSWORD = "USER_PASSWORD_INCORRECT";

    private static final Map<String, List<KnownErrorResponse>> KNOWN_ERRORS =
            ImmutableMap.<String, List<KnownErrorResponse>>builder()
                    .put(
                            INCORRECT_PASSWORD,
                            Arrays.asList(
                                    KnownErrorResponse.withPattern(
                                            INCORRECT_PASSWORD_PATTERN,
                                            new InvalidCredentialsError()),
                                    KnownErrorResponse.withPattern(
                                            LAST_ATTEMPT_PATTERN,
                                            // set some new error to indicate last attempt!
                                            new InvalidCredentialsError())))
                    .put(
                            "USER_NOT_FOUND",
                            Collections.singletonList(
                                    KnownErrorResponse.withoutMessage(
                                            new ThirdPartyAppNoClientError())))
                    .build();

    public static AgentBankApiError toKnownErrorFromResponseOrDefault(
            HttpResponseException e, AgentBankApiError defaultError) {
        if (!e.getResponse().hasBody()) {
            return defaultError;
        }

        ErrorResponse errorResponse = getBodyAsExpectedType(e.getResponse());
        if (errorResponse != null && KNOWN_ERRORS.containsKey(errorResponse.getReasonCode())) {
            List<KnownErrorResponse> knownErrorResponses =
                    KNOWN_ERRORS.get(errorResponse.getReasonCode());

            for (KnownErrorResponse knownErrorResponse : knownErrorResponses) {
                if (displayMessagesAreEqual(errorResponse, knownErrorResponse)
                        || displayMessageMatchesPattern(
                                errorResponse, knownErrorResponse.getPattern())) {
                    return knownErrorResponse.getErrorToReturn();
                }
            }
        }
        return defaultError;
    }

    private static boolean displayMessagesAreEqual(
            ErrorResponse errorResponse, KnownErrorResponse knownErrorResponse) {
        return knownErrorResponse.getPattern() == null
                && Objects.equals(
                        knownErrorResponse.getReasonDisplayMessage(),
                        errorResponse.getReasonDisplayMessage());
    }

    private static boolean displayMessageMatchesPattern(
            ErrorResponse errorResponse, Pattern knownErrorMessagePattern) {
        return knownErrorMessagePattern != null
                && StringUtils.isNotBlank(errorResponse.getReasonDisplayMessage())
                && knownErrorMessagePattern
                        .matcher(errorResponse.getReasonDisplayMessage())
                        .matches();
    }

    private static ErrorResponse getBodyAsExpectedType(HttpResponse response) {
        try {
            return response.getBody(ErrorResponse.class);
        } catch (RuntimeException e) {
            return null;
        }
    }

    public static AgentBankApiError toErrorFromLoginException(LoginException e) {
        switch (e.getError()) {
            case CREDENTIALS_VERIFICATION_ERROR:
                log.error(e.getMessage());
                return new AuthorizationError();
            case INCORRECT_CREDENTIALS:
                log.error(e.getMessage());
                return new InvalidCredentialsError();
            case DEFAULT_MESSAGE:
                log.error(e.getMessage());
                return new ThirdPartyAppUnknownError();
            default:
                return new ThirdPartyAppUnknownError();
        }
    }

    public static AgentBankApiError toErrorFromNemIdException(NemIdException e) {
        switch (e.getError()) {
            case REJECTED:
                log.error(e.getError().name());
                return new ThirdPartyAppCancelledError();
            case TIMEOUT:
                log.error(e.getError().name());
                return new ThirdPartyAppTimedOutError();
            case CODEAPP_NOT_REGISTERED:
                log.error(e.getMessage());
                return new AuthorizationError();
            default:
                return new ThirdPartyAppUnknownError();
        }
    }

    public static AgentBankApiError toErrorFromSupplementalInfoException(
            SupplementalInfoException e) {
        if (e.getError() == SupplementalInfoError.WAIT_TIMEOUT) {
            return new NoUserInteractionResponseError();
        }
        throw e;
    }

    public static AgentFailedAuthenticationResult getSignInFailedAuthResult(
            AgentProceedNextStepAuthenticationRequest request,
            HttpResponseException e,
            boolean isAutoAuth) {
        if (isAutoAuth) {
            log.error("Failed to signIn to Lunar during autoAuth", e);
            return new AgentFailedAuthenticationResult(
                    new SessionExpiredError(), request.getAuthenticationPersistedData());
        }
        log.error("Failed to signIn to Lunar", e);
        return new AgentFailedAuthenticationResult(
                LunarAuthenticationExceptionHandler.toKnownErrorFromResponseOrDefault(
                        e, new AccessTokenFetchingFailureError()),
                request.getAuthenticationPersistedData());
    }
}
