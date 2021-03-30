package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.exception;

import agents_platform_framework.org.springframework.web.server.ResponseStatusException;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.agentplatform.authentication.result.error.LastAttemptError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarAuthDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.exception.ErrorResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AccountBlockedError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AuthorizationError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.InvalidCredentialsError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ServerError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.SessionExpiredError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ThirdPartyAppNoClientError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ThirdPartyAppUnknownError;
import se.tink.backend.aggregation.agentsplatform.framework.error.Error;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Slf4j
public class LunarAuthExceptionHandler {

    private static final String INCORRECT_PASSWORD = "USER_PASSWORD_INCORRECT";
    private static final String LAST_ATTEMPT = "USER_PASSWORD_INCORRECT_RESET_APP";
    private static final String NOT_A_LUNAR_USER = "USER_NOT_FOUND";
    private static final String TOKEN_REVOKED = "ACCESS_TOKEN_REVOKED_BY_CUSTOMER_SUPPORT";
    private static final String NEMID_WRONG_TYPE = "NEMID_WRONG_TYPE";
    private static final LocalizableKey USE_YOUR_PRIVATE_NEMID =
            new LocalizableKey("Please use your private NemID to log in.");

    private static final Map<String, AgentBankApiError> KNOWN_ERRORS =
            ImmutableMap.<String, AgentBankApiError>builder()
                    .put(INCORRECT_PASSWORD, new InvalidCredentialsError())
                    .put(LAST_ATTEMPT, new InvalidCredentialsError(LastAttemptError.getError()))
                    .put(
                            NOT_A_LUNAR_USER,
                            new ThirdPartyAppNoClientError(
                                    getErrorWithChangedUserMessage(
                                            AgentError.THIRD_PARTY_APP_NO_CLIENT.getCode(),
                                            LoginError.NOT_CUSTOMER.userMessage().get())))
                    .put(
                            TOKEN_REVOKED,
                            new AccountBlockedError(
                                    getErrorWithChangedUserMessage(
                                            AgentError.ACCOUNT_BLOCKED.getCode(),
                                            LoginError.INVALIDATED_CREDENTIALS
                                                    .userMessage()
                                                    .get())))
                    .put(
                            NEMID_WRONG_TYPE,
                            new ThirdPartyAppUnknownError(
                                    getErrorWithChangedUserMessage(
                                            AgentError.THIRD_PARTY_APP_UNKNOWN_ERROR.getCode(),
                                            USE_YOUR_PRIVATE_NEMID.get())))
                    .build();

    private static Error getErrorWithChangedUserMessage(String errorCode, String errorMessage) {
        return Error.builder()
                .uniqueId(UUID.randomUUID().toString())
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build();
    }

    public static AgentBankApiError toKnownErrorFromResponseOrDefault(
            ResponseStatusException e, AgentBankApiError defaultError) {
        if (isBankSideFailure(e)) {
            return getServerError(e.getStatus().value());
        }

        ErrorResponse errorResponse = deserializeBodyToErrorResponse(e.getReason());
        if (errorResponse != null && KNOWN_ERRORS.containsKey(errorResponse.getReasonCode())) {
            return KNOWN_ERRORS.get(errorResponse.getReasonCode());
        }
        return defaultError;
    }

    private static boolean isBankSideFailure(ResponseStatusException e) {
        return e.getStatus().is5xxServerError();
    }

    private static AgentBankApiError getServerError(int status) {
        return new ServerError(toErrorDetailsFromStatus(status));
    }

    private static Error toErrorDetailsFromStatus(int status) {
        switch (status) {
            case 500:
                return getErrorWithChangedUserMessage(
                        AgentError.HTTP_RESPONSE_ERROR.getCode(),
                        BankServiceError.BANK_SIDE_FAILURE.userMessage().get());
            case 502:
            case 503:
            case 504:
                return getErrorWithChangedUserMessage(
                        AgentError.HTTP_RESPONSE_ERROR.getCode(),
                        BankServiceError.NO_BANK_SERVICE.userMessage().get());
            default:
                return new ServerError().getDetails();
        }
    }

    private static ErrorResponse deserializeBodyToErrorResponse(String response) {
        return SerializationUtils.deserializeFromString(response, ErrorResponse.class);
    }

    public static AgentFailedAuthenticationResult getFetchAccountsFailedResult(
            LunarAuthDataAccessor authDataAccessor, ResponseStatusException e, boolean isAutoAuth) {
        if (isAutoAuth) {
            log.error("Failed to confirm login by fetching Lunar accounts during autoAuth!", e);
            return new AgentFailedAuthenticationResult(
                    LunarAuthExceptionHandler.toKnownErrorFromResponseOrDefault(
                            e, new SessionExpiredError()),
                    authDataAccessor.clearData());
        }
        log.error("Failed to confirm login by fetching Lunar accounts!", e);
        return new AgentFailedAuthenticationResult(
                LunarAuthExceptionHandler.toKnownErrorFromResponseOrDefault(
                        e, new AuthorizationError()),
                authDataAccessor.clearData());
    }
}
