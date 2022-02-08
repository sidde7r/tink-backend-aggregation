package src.libraries.connectivity_errors;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import se.tink.backend.aggregation.agents.agentplatform.authentication.result.error.AgentPlatformAuthenticationProcessException;
import se.tink.backend.aggregation.agents.agentplatform.authentication.result.error.NoUserInteractionResponseError;
import se.tink.backend.aggregation.agents.agentplatform.authentication.result.error.UserInteractionAbortedError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AccessTokenFetchingFailureError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AccountBlockedError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AuthenticationError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AuthorizationError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.DeviceRegistrationError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.IncorrectCardReaderResponseCodeError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.IncorrectOtpError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.InvalidCredentialsError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.InvalidRequestError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.InvalidScopeError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.RefreshTokenFailureError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ServerError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ServerTemporaryUnavailableError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.SessionExpiredError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ThirdPartyAppAlreadyInProgressError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ThirdPartyAppCancelledError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ThirdPartyAppNoClientError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ThirdPartyAppTimedOutError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ThirdPartyAppUnknownError;
import se.tink.connectivity.errors.ConnectivityError;
import se.tink.connectivity.errors.ConnectivityErrorDetails;

class AgentsPlatformToConnectivityErrorCreator
        implements LegacyExceptionToConnectivityErrorMapper.ConnectivityErrorCreator {

    private static final ImmutableMap<Class<? extends AgentBankApiError>, ConnectivityError>
            AP_ERROR_TO_TO_CONNECTIVITY_MAP =
                    ImmutableMap.<Class<? extends AgentBankApiError>, ConnectivityError>builder()
                            .put(
                                    AccountBlockedError.class,
                                    ConnectivityErrorFactory.userLoginError(
                                            ConnectivityErrorDetails.UserLoginErrors.USER_BLOCKED))
                            .put(
                                    InvalidCredentialsError.class,
                                    ConnectivityErrorFactory.userLoginError(
                                            ConnectivityErrorDetails.UserLoginErrors
                                                    .STATIC_CREDENTIALS_INCORRECT))
                            .put(
                                    InvalidRequestError.class,
                                    ConnectivityErrorFactory.tinkSideError(
                                            ConnectivityErrorDetails.TinkSideErrors
                                                    .TINK_INTERNAL_SERVER_ERROR))
                            .put(
                                    InvalidScopeError.class,
                                    ConnectivityErrorFactory.tinkSideError(
                                            ConnectivityErrorDetails.TinkSideErrors
                                                    .TINK_INTERNAL_SERVER_ERROR))
                            .put(
                                    AccessTokenFetchingFailureError.class,
                                    ConnectivityErrorFactory.authorizationError(
                                            ConnectivityErrorDetails.AuthorizationErrors
                                                    .SESSION_EXPIRED))
                            .put(
                                    RefreshTokenFailureError.class,
                                    ConnectivityErrorFactory.authorizationError(
                                            ConnectivityErrorDetails.AuthorizationErrors
                                                    .SESSION_EXPIRED))
                            .put(
                                    SessionExpiredError.class,
                                    ConnectivityErrorFactory.authorizationError(
                                            ConnectivityErrorDetails.AuthorizationErrors
                                                    .SESSION_EXPIRED))
                            .put(
                                    ServerError.class,
                                    ConnectivityErrorFactory.providerError(
                                            ConnectivityErrorDetails.ProviderErrors
                                                    .PROVIDER_UNAVAILABLE))
                            .put(
                                    ServerTemporaryUnavailableError.class,
                                    ConnectivityErrorFactory.providerError(
                                            ConnectivityErrorDetails.ProviderErrors
                                                    .PROVIDER_UNAVAILABLE))
                            .put(
                                    ThirdPartyAppAlreadyInProgressError.class,
                                    ConnectivityErrorFactory.userLoginError(
                                            ConnectivityErrorDetails.UserLoginErrors
                                                    .USER_CONCURRENT_LOGINS))
                            .put(
                                    ThirdPartyAppCancelledError.class,
                                    ConnectivityErrorFactory.userLoginError(
                                            ConnectivityErrorDetails.UserLoginErrors
                                                    .DYNAMIC_CREDENTIALS_FLOW_CANCELLED))
                            .put(
                                    DeviceRegistrationError.class,
                                    ConnectivityErrorFactory.userLoginError(
                                            ConnectivityErrorDetails.UserLoginErrors
                                                    .DYNAMIC_CREDENTIALS_INCORRECT))
                            .put(
                                    IncorrectCardReaderResponseCodeError.class,
                                    ConnectivityErrorFactory.userLoginError(
                                            ConnectivityErrorDetails.UserLoginErrors
                                                    .DYNAMIC_CREDENTIALS_INCORRECT))
                            .put(
                                    IncorrectOtpError.class,
                                    ConnectivityErrorFactory.userLoginError(
                                            ConnectivityErrorDetails.UserLoginErrors
                                                    .DYNAMIC_CREDENTIALS_INCORRECT))
                            .put(
                                    ThirdPartyAppNoClientError.class,
                                    ConnectivityErrorFactory.userLoginError(
                                            ConnectivityErrorDetails.UserLoginErrors
                                                    .DYNAMIC_CREDENTIALS_INCORRECT))
                            .put(
                                    ThirdPartyAppUnknownError.class,
                                    ConnectivityErrorFactory.userLoginError(
                                            ConnectivityErrorDetails.UserLoginErrors
                                                    .DYNAMIC_CREDENTIALS_INCORRECT))
                            .put(
                                    ThirdPartyAppTimedOutError.class,
                                    ConnectivityErrorFactory.userLoginError(
                                            ConnectivityErrorDetails.UserLoginErrors
                                                    .DYNAMIC_CREDENTIALS_FLOW_TIMEOUT))
                            .put(
                                    NoUserInteractionResponseError.class,
                                    ConnectivityErrorFactory.userLoginError(
                                            ConnectivityErrorDetails.UserLoginErrors
                                                    .DYNAMIC_CREDENTIALS_FLOW_TIMEOUT))
                            .put(
                                    UserInteractionAbortedError.class,
                                    ConnectivityErrorFactory.userLoginError(
                                            ConnectivityErrorDetails.UserLoginErrors
                                                    .DYNAMIC_CREDENTIALS_FLOW_TIMEOUT))
                            .put(
                                    AuthenticationError.class,
                                    ConnectivityErrorFactory.userLoginError(
                                            ConnectivityErrorDetails.UserLoginErrors.UNRECOGNIZED))
                            .put(
                                    AuthorizationError.class,
                                    ConnectivityErrorFactory.authorizationError(
                                            ConnectivityErrorDetails.AuthorizationErrors
                                                    .UNRECOGNIZED))
                            .build();

    @Override
    public Optional<ConnectivityError> tryCreateConnectivityErrorForException(Exception exception) {
        if (exception instanceof AgentPlatformAuthenticationProcessException) {
            AgentBankApiError agentPlatformError =
                    ((AgentPlatformAuthenticationProcessException) exception)
                            .getSourceAgentPlatformError();
            return Optional.ofNullable(
                            AP_ERROR_TO_TO_CONNECTIVITY_MAP.get(agentPlatformError.getClass()))
                    .map(ConnectivityError::toBuilder)
                    .map(ConnectivityError.Builder::build);
        }

        return Optional.empty();
    }
}
