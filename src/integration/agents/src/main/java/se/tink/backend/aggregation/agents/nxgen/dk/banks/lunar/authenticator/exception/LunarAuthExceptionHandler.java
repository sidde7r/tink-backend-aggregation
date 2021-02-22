package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.exception;

import agents_platform_framework.org.springframework.web.server.ResponseStatusException;
import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.agentplatform.authentication.result.error.LastAttemptError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarAuthDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.exception.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.exception.KnownErrorResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AccessTokenFetchingFailureError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AccountBlockedError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.InvalidCredentialsError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.SessionExpiredError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ThirdPartyAppNoClientError;
import se.tink.backend.aggregation.agentsplatform.framework.error.Error;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Slf4j
public class LunarAuthExceptionHandler {

    private static final Pattern INCORRECT_PASSWORD_PATTERN =
            Pattern.compile("You have [2345] attempts left\\.");
    private static final String LAST_ATTEMPT_MESSAGE =
            "If you enter a wrong PIN again, your access to the app is blocked. You will then have to use your NemID to sign in again.";

    private static final String INCORRECT_PASSWORD = "USER_PASSWORD_INCORRECT";
    private static final String LAST_ATTEMPT = "USER_PASSWORD_INCORRECT_RESET_APP";
    private static final String NOT_A_LUNAR_USER = "USER_NOT_FOUND";
    private static final String TOKEN_REVOKED = "ACCESS_TOKEN_REVOKED_BY_CUSTOMER_SUPPORT";

    private static final Map<String, List<KnownErrorResponse>> KNOWN_ERRORS =
            ImmutableMap.<String, List<KnownErrorResponse>>builder()
                    .put(
                            INCORRECT_PASSWORD,
                            Collections.singletonList(
                                    KnownErrorResponse.withPattern(
                                            INCORRECT_PASSWORD_PATTERN,
                                            new InvalidCredentialsError())))
                    .put(
                            LAST_ATTEMPT,
                            Collections.singletonList(
                                    KnownErrorResponse.withMessage(
                                            LAST_ATTEMPT_MESSAGE,
                                            new InvalidCredentialsError(
                                                    LastAttemptError.getError()))))
                    .put(
                            NOT_A_LUNAR_USER,
                            Collections.singletonList(
                                    KnownErrorResponse.withoutMessage(
                                            new ThirdPartyAppNoClientError(
                                                    getErrorWithChangedUserMessage(
                                                            AgentError.THIRD_PARTY_APP_NO_CLIENT
                                                                    .getCode(),
                                                            LoginError.NOT_CUSTOMER)))))
                    .put(
                            TOKEN_REVOKED,
                            Collections.singletonList(
                                    KnownErrorResponse.withoutMessage(
                                            new AccountBlockedError(
                                                    getErrorWithChangedUserMessage(
                                                            AgentError.ACCOUNT_BLOCKED.getCode(),
                                                            LoginError.INVALIDATED_CREDENTIALS)))))
                    .build();

    private static Error getErrorWithChangedUserMessage(
            String errorCode, se.tink.backend.aggregation.agents.exceptions.agent.AgentError e) {
        return Error.builder()
                .uniqueId(UUID.randomUUID().toString())
                .errorCode(errorCode)
                .errorMessage(e.userMessage().get())
                .build();
    }

    public static AgentBankApiError toKnownErrorFromResponseOrDefault(
            ResponseStatusException e, AgentBankApiError defaultError) {
        if (StringUtils.isBlank(e.getReason())) {
            return defaultError;
        }

        ErrorResponse errorResponse = deserializeBodyToErrorResponse(e.getReason());
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

    private static ErrorResponse deserializeBodyToErrorResponse(String response) {
        return SerializationUtils.deserializeFromString(response, ErrorResponse.class);
    }

    public static AgentFailedAuthenticationResult getSignInFailedAuthResult(
            LunarAuthDataAccessor authDataAccessor, ResponseStatusException e, boolean isAutoAuth) {
        if (isAutoAuth) {
            log.error("Failed to signIn to Lunar during autoAuth", e);
            return new AgentFailedAuthenticationResult(
                    LunarAuthExceptionHandler.toKnownErrorFromResponseOrDefault(
                            e, new SessionExpiredError()),
                    authDataAccessor.clearData());
        }
        log.error("Failed to signIn to Lunar", e);
        return new AgentFailedAuthenticationResult(
                LunarAuthExceptionHandler.toKnownErrorFromResponseOrDefault(
                        e, new AccessTokenFetchingFailureError()),
                authDataAccessor.clearData());
    }
}
