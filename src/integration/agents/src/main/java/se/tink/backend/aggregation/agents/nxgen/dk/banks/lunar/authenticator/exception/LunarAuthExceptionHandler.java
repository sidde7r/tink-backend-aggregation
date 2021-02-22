package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.exception;

import agents_platform_framework.org.springframework.web.server.ResponseStatusException;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.agentplatform.authentication.result.error.LastAttemptError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarAuthDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.exception.ErrorResponse;
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

    private static final String INCORRECT_PASSWORD = "USER_PASSWORD_INCORRECT";
    private static final String LAST_ATTEMPT = "USER_PASSWORD_INCORRECT_RESET_APP";
    private static final String NOT_A_LUNAR_USER = "USER_NOT_FOUND";
    private static final String TOKEN_REVOKED = "ACCESS_TOKEN_REVOKED_BY_CUSTOMER_SUPPORT";

    private static final Map<String, AgentBankApiError> KNOWN_ERRORS =
            ImmutableMap.<String, AgentBankApiError>builder()
                    .put(INCORRECT_PASSWORD, new InvalidCredentialsError())
                    .put(LAST_ATTEMPT, new InvalidCredentialsError(LastAttemptError.getError()))
                    .put(
                            NOT_A_LUNAR_USER,
                            new ThirdPartyAppNoClientError(
                                    getErrorWithChangedUserMessage(
                                            AgentError.THIRD_PARTY_APP_NO_CLIENT.getCode(),
                                            LoginError.NOT_CUSTOMER)))
                    .put(
                            TOKEN_REVOKED,
                            new AccountBlockedError(
                                    getErrorWithChangedUserMessage(
                                            AgentError.ACCOUNT_BLOCKED.getCode(),
                                            LoginError.INVALIDATED_CREDENTIALS)))
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
            return KNOWN_ERRORS.get(errorResponse.getReasonCode());
        }
        return defaultError;
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
