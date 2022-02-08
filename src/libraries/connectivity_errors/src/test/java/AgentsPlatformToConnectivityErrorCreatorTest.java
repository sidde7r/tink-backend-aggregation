package src.libraries.connectivity_errors;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.agentplatform.authentication.result.error.AgentPlatformAuthenticationProcessError;
import se.tink.backend.aggregation.agents.agentplatform.authentication.result.error.AgentPlatformAuthenticationProcessException;
import se.tink.backend.aggregation.agents.agentplatform.authentication.result.error.NoUserInteractionResponseError;
import se.tink.backend.aggregation.agents.agentplatform.authentication.result.error.UserInteractionAbortedError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AccessTokenFetchingFailureError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AccountBlockedError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AuthenticationError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AuthorizationError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.DeviceRegistrationError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.FetchDataError;
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

@RunWith(JUnitParamsRunner.class)
public class AgentsPlatformToConnectivityErrorCreatorTest {

    private static final String MESSAGE = "xyz";

    private final AgentsPlatformToConnectivityErrorCreator creator =
            new AgentsPlatformToConnectivityErrorCreator();

    @Test
    public void shouldReturnOptionalEmptyIfNotAgentsPlatformExceptionProvided() {
        // given
        RuntimeException e = new RuntimeException();
        // when
        Optional<ConnectivityError> connectivityError =
                creator.tryCreateConnectivityErrorForException(e);
        // then
        assertThat(connectivityError).isEmpty();
    }

    @Test
    // fetchDataError is currently not mapped as it touches fetch part not auth, which we do not
    // support
    public void shouldReturnOptionalEmptyIfNotMappedErrorIsPassed() {
        // given
        FetchDataError fetchDataError = new FetchDataError(null);
        AgentPlatformAuthenticationProcessError error =
                new AgentPlatformAuthenticationProcessError(fetchDataError);
        AgentPlatformAuthenticationProcessException e =
                new AgentPlatformAuthenticationProcessException(error, MESSAGE);
        // when
        Optional<ConnectivityError> connectivityError =
                creator.tryCreateConnectivityErrorForException(e);
        // then
        assertThat(connectivityError).isEmpty();
    }

    @Test
    public void shouldReturnUserBlockedErrorIfAccountBlockedProvided() {
        // given
        AccountBlockedError accountBlockedError = new AccountBlockedError();
        AgentPlatformAuthenticationProcessError error =
                new AgentPlatformAuthenticationProcessError(accountBlockedError);
        AgentPlatformAuthenticationProcessException e =
                new AgentPlatformAuthenticationProcessException(error, MESSAGE);
        // when
        Optional<ConnectivityError> connectivityError =
                creator.tryCreateConnectivityErrorForException(e);
        // then
        assertThat(connectivityError)
                .isEqualTo(
                        Optional.of(
                                ConnectivityErrorFactory.userLoginError(
                                                ConnectivityErrorDetails.UserLoginErrors
                                                        .USER_BLOCKED)
                                        .toBuilder()
                                        .build()));
    }

    @Test
    public void shouldReturnStaticCredentialsErrorIfInvalidCredentialsProvided() {
        // given
        InvalidCredentialsError invalidCredentialsError = new InvalidCredentialsError();
        AgentPlatformAuthenticationProcessError error =
                new AgentPlatformAuthenticationProcessError(invalidCredentialsError);
        AgentPlatformAuthenticationProcessException e =
                new AgentPlatformAuthenticationProcessException(error, MESSAGE);
        // when
        Optional<ConnectivityError> connectivityError =
                creator.tryCreateConnectivityErrorForException(e);
        // then
        assertThat(connectivityError)
                .isEqualTo(
                        Optional.of(
                                ConnectivityErrorFactory.userLoginError(
                                                ConnectivityErrorDetails.UserLoginErrors
                                                        .STATIC_CREDENTIALS_INCORRECT)
                                        .toBuilder()
                                        .build()));
    }

    @Test
    @Parameters(method = "bankServerErrors")
    public void shouldReturnProviderUnavailableErrorIfServerErrorProvided(
            AgentBankApiError agentBankApiError) {
        // given
        AgentPlatformAuthenticationProcessError error =
                new AgentPlatformAuthenticationProcessError(agentBankApiError);
        AgentPlatformAuthenticationProcessException e =
                new AgentPlatformAuthenticationProcessException(error, MESSAGE);
        // when
        Optional<ConnectivityError> connectivityError =
                creator.tryCreateConnectivityErrorForException(e);
        // then
        assertThat(connectivityError)
                .isEqualTo(
                        Optional.of(
                                ConnectivityErrorFactory.providerError(
                                                ConnectivityErrorDetails.ProviderErrors
                                                        .PROVIDER_UNAVAILABLE)
                                        .toBuilder()
                                        .build()));
    }

    @SuppressWarnings("unused")
    private AgentBankApiError[] bankServerErrors() {
        return new AgentBankApiError[] {new ServerError(), new ServerTemporaryUnavailableError()};
    }

    @Test
    public void shouldReturnUserConcurrentLoginsErrorIfThirdPartyAppAlreadyInProgressProvided() {
        // given
        ThirdPartyAppAlreadyInProgressError alreadyInProgressError =
                new ThirdPartyAppAlreadyInProgressError();
        AgentPlatformAuthenticationProcessError error =
                new AgentPlatformAuthenticationProcessError(alreadyInProgressError);
        AgentPlatformAuthenticationProcessException e =
                new AgentPlatformAuthenticationProcessException(error, MESSAGE);
        // when
        Optional<ConnectivityError> connectivityError =
                creator.tryCreateConnectivityErrorForException(e);
        // then
        assertThat(connectivityError)
                .isEqualTo(
                        Optional.of(
                                ConnectivityErrorFactory.userLoginError(
                                                ConnectivityErrorDetails.UserLoginErrors
                                                        .USER_CONCURRENT_LOGINS)
                                        .toBuilder()
                                        .build()));
    }

    @Test
    public void shouldReturnDynamicCredentialsFlowCancelledErrorIfThirdPartyAppCancelledProvided() {
        // given
        ThirdPartyAppCancelledError alreadyInProgressError = new ThirdPartyAppCancelledError();
        AgentPlatformAuthenticationProcessError error =
                new AgentPlatformAuthenticationProcessError(alreadyInProgressError);
        AgentPlatformAuthenticationProcessException e =
                new AgentPlatformAuthenticationProcessException(error, MESSAGE);
        // when
        Optional<ConnectivityError> connectivityError =
                creator.tryCreateConnectivityErrorForException(e);
        // then
        assertThat(connectivityError)
                .isEqualTo(
                        Optional.of(
                                ConnectivityErrorFactory.userLoginError(
                                                ConnectivityErrorDetails.UserLoginErrors
                                                        .DYNAMIC_CREDENTIALS_FLOW_CANCELLED)
                                        .toBuilder()
                                        .build()));
    }

    @Test
    @Parameters(method = "internalServerErrors")
    public void shouldReturnInternalServerErrorIfInternalServerErrorsProvided(
            AgentBankApiError agentBankApiError) {
        // given
        AgentPlatformAuthenticationProcessError error =
                new AgentPlatformAuthenticationProcessError(agentBankApiError);
        AgentPlatformAuthenticationProcessException e =
                new AgentPlatformAuthenticationProcessException(error, MESSAGE);
        // when
        Optional<ConnectivityError> connectivityError =
                creator.tryCreateConnectivityErrorForException(e);
        // then
        assertThat(connectivityError)
                .isEqualTo(
                        Optional.of(
                                ConnectivityErrorFactory.tinkSideError(
                                                ConnectivityErrorDetails.TinkSideErrors
                                                        .TINK_INTERNAL_SERVER_ERROR)
                                        .toBuilder()
                                        .build()));
    }

    @SuppressWarnings("unused")
    private AgentBankApiError[] internalServerErrors() {
        return new AgentBankApiError[] {new InvalidRequestError(), new InvalidScopeError()};
    }

    @Test
    @Parameters(method = "sessionExpiredErrors")
    public void shouldReturnSessionExpiredIfSessionExpiredErrorsProvided(
            AgentBankApiError agentBankApiError) {
        // given
        AgentPlatformAuthenticationProcessError error =
                new AgentPlatformAuthenticationProcessError(agentBankApiError);
        AgentPlatformAuthenticationProcessException e =
                new AgentPlatformAuthenticationProcessException(error, MESSAGE);
        // when
        Optional<ConnectivityError> connectivityError =
                creator.tryCreateConnectivityErrorForException(e);
        // then
        assertThat(connectivityError)
                .isEqualTo(
                        Optional.of(
                                ConnectivityErrorFactory.authorizationError(
                                                ConnectivityErrorDetails.AuthorizationErrors
                                                        .SESSION_EXPIRED)
                                        .toBuilder()
                                        .build()));
    }

    @SuppressWarnings("unused")
    private AgentBankApiError[] sessionExpiredErrors() {
        return new AgentBankApiError[] {
            new AccessTokenFetchingFailureError(),
            new RefreshTokenFailureError(),
            new SessionExpiredError()
        };
    }

    @Test
    @Parameters(method = "dynamicCredentialsErrors")
    public void shouldReturnDynamicCredentialsIncorrectIfDynamicCredentialsErrorsProvided(
            AgentBankApiError agentBankApiError) {
        // given
        AgentPlatformAuthenticationProcessError error =
                new AgentPlatformAuthenticationProcessError(agentBankApiError);
        AgentPlatformAuthenticationProcessException e =
                new AgentPlatformAuthenticationProcessException(error, MESSAGE);
        // when
        Optional<ConnectivityError> connectivityError =
                creator.tryCreateConnectivityErrorForException(e);
        // then
        assertThat(connectivityError)
                .isEqualTo(
                        Optional.of(
                                ConnectivityErrorFactory.userLoginError(
                                                ConnectivityErrorDetails.UserLoginErrors
                                                        .DYNAMIC_CREDENTIALS_INCORRECT)
                                        .toBuilder()
                                        .build()));
    }

    @SuppressWarnings("unused")
    private AgentBankApiError[] dynamicCredentialsErrors() {
        return new AgentBankApiError[] {
            new DeviceRegistrationError(),
            new IncorrectCardReaderResponseCodeError(),
            new IncorrectOtpError(),
            new ThirdPartyAppNoClientError(),
            new ThirdPartyAppUnknownError()
        };
    }

    @Test
    @Parameters(method = "dynamicCredentialsTimeoutErrors")
    public void shouldReturnDynamicCredentialsTimeoutIfDynamicCredentialsTimeoutErrorsProvided(
            AgentBankApiError agentBankApiError) {
        // given
        AgentPlatformAuthenticationProcessError error =
                new AgentPlatformAuthenticationProcessError(agentBankApiError);
        AgentPlatformAuthenticationProcessException e =
                new AgentPlatformAuthenticationProcessException(error, MESSAGE);
        // when
        Optional<ConnectivityError> connectivityError =
                creator.tryCreateConnectivityErrorForException(e);
        // then
        assertThat(connectivityError)
                .isEqualTo(
                        Optional.of(
                                ConnectivityErrorFactory.userLoginError(
                                                ConnectivityErrorDetails.UserLoginErrors
                                                        .DYNAMIC_CREDENTIALS_FLOW_TIMEOUT)
                                        .toBuilder()
                                        .build()));
    }

    @SuppressWarnings("unused")
    private AgentBankApiError[] dynamicCredentialsTimeoutErrors() {
        return new AgentBankApiError[] {
            new ThirdPartyAppTimedOutError(),
            new NoUserInteractionResponseError(),
            new UserInteractionAbortedError()
        };
    }

    @Test
    public void shouldReturnProviderUnrecognizedIfGeneralAuthenticationErrorProvided() {
        // given
        AuthenticationError authenticationError = new AuthenticationError(null);
        AgentPlatformAuthenticationProcessError error =
                new AgentPlatformAuthenticationProcessError(authenticationError);
        AgentPlatformAuthenticationProcessException e =
                new AgentPlatformAuthenticationProcessException(error, MESSAGE);
        // when
        Optional<ConnectivityError> connectivityError =
                creator.tryCreateConnectivityErrorForException(e);
        // then
        assertThat(connectivityError)
                .isEqualTo(
                        Optional.of(
                                ConnectivityErrorFactory.userLoginError(
                                                ConnectivityErrorDetails.UserLoginErrors
                                                        .UNRECOGNIZED)
                                        .toBuilder()
                                        .build()));
    }

    @Test
    public void shouldReturnProviderUnrecognizedIfGeneralAuthorizationErrorProvided() {
        // given
        AuthorizationError authorizationError = new AuthorizationError();
        AgentPlatformAuthenticationProcessError error =
                new AgentPlatformAuthenticationProcessError(authorizationError);
        AgentPlatformAuthenticationProcessException e =
                new AgentPlatformAuthenticationProcessException(error, MESSAGE);
        // when
        Optional<ConnectivityError> connectivityError =
                creator.tryCreateConnectivityErrorForException(e);
        // then
        assertThat(connectivityError)
                .isEqualTo(
                        Optional.of(
                                ConnectivityErrorFactory.authorizationError(
                                                ConnectivityErrorDetails.AuthorizationErrors
                                                        .UNRECOGNIZED)
                                        .toBuilder()
                                        .build()));
    }
}
