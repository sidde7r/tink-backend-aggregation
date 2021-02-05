package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import agents_platform_framework.org.springframework.http.HttpStatus;
import agents_platform_framework.org.springframework.web.server.ResponseStatusException;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.agentplatform.authentication.ObjectMapperFactory;
import se.tink.backend.aggregation.agents.agentplatform.authentication.result.error.LastAttemptError;
import se.tink.backend.aggregation.agents.agentplatform.authentication.result.error.NoUserInteractionResponseError;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarAuthData;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarAuthDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.steps.StepsUtils;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AccessTokenFetchingFailureError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AccountBlockedError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AuthorizationError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.InvalidCredentialsError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.SessionExpiredError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ThirdPartyAppCancelledError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ThirdPartyAppNoClientError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ThirdPartyAppTimedOutError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ThirdPartyAppUnknownError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception.NemIdError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception.NemIdException;

@RunWith(JUnitParamsRunner.class)
public class AuthenticationExceptionHandlerTest {

    @Test
    @Parameters(method = "knownErrorsParams")
    public void shouldMapToKnownErrorFromResponseOrDefault(
            ResponseStatusException e, AgentBankApiError expected) {
        // given & when
        AgentBankApiError result =
                AuthenticationExceptionHandler.toKnownErrorFromResponseOrDefault(
                        e, new AuthorizationError());

        // then
        assertThat(result.getClass()).isEqualTo(expected.getClass());
        assertThat(result.getDetails())
                .isEqualToIgnoringGivenFields(expected.getDetails(), "uniqueId");
    }

    private Object[] knownErrorsParams() {
        return new Object[] {
            new Object[] {
                new ResponseStatusException(HttpStatus.BAD_REQUEST), new AuthorizationError()
            },
            new Object[] {
                new ResponseStatusException(HttpStatus.BAD_REQUEST, ""), new AuthorizationError()
            },
            new Object[] {
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "test"),
                new AuthorizationError()
            },
            new Object[] {
                new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "{\"reasonCode\": \"UNKNOWN_REASON_CODE\"}"),
                new AuthorizationError()
            },
            new Object[] {
                new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "{\"reasonCode\": \"USER_PASSWORD_INCORRECT\"}"),
                new AuthorizationError()
            },
            new Object[] {
                new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "{\"reasonCode\": \"USER_PASSWORD_INCORRECT\", \"reasonDisplayMessage\": \"You have 1 attempts left.\"}"),
                new AuthorizationError()
            },
            new Object[] {
                new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "{\"reasonCode\": \"USER_PASSWORD_INCORRECT\", \"reasonDisplayMessage\": \"You have 2 attempts left.\"}"),
                new InvalidCredentialsError()
            },
            new Object[] {
                new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "{\"reasonCode\": \"USER_PASSWORD_INCORRECT\", \"reasonDisplayMessage\": \"You have 3 attempts left.\"}"),
                new InvalidCredentialsError()
            },
            new Object[] {
                new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "{\"reasonCode\": \"USER_PASSWORD_INCORRECT\", \"reasonDisplayMessage\": \"You have 4 attempts left.\"}"),
                new InvalidCredentialsError()
            },
            new Object[] {
                new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "{\"reasonCode\": \"USER_PASSWORD_INCORRECT\", \"reasonDisplayMessage\": \"You have 5 attempts left.\"}"),
                new InvalidCredentialsError()
            },
            new Object[] {
                new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "{\"reasonCode\": \"USER_PASSWORD_INCORRECT_RESET_APP\"}"),
                new AuthorizationError()
            },
            new Object[] {
                new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "{\"reasonCode\": \"USER_PASSWORD_INCORRECT_RESET_APP\", \"reasonDisplayMessage\": \"Wrong text\"}"),
                new AuthorizationError()
            },
            new Object[] {
                new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "{\"reasonCode\": \"USER_PASSWORD_INCORRECT_RESET_APP\", \"reasonDisplayMessage\": \"If you enter a wrong PIN again, your access to the app is blocked. You will then have to use your NemID to sign in again.\"}"),
                new InvalidCredentialsError(LastAttemptError.getError())
            },
            new Object[] {
                new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "{\"reasonCode\": \"USER_NOT_FOUND\"}"),
                new ThirdPartyAppNoClientError()
            },
            new Object[] {
                new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "{\"reasonCode\": \"ACCESS_TOKEN_REVOKED_BY_CUSTOMER_SUPPORT\"}"),
                new AccountBlockedError()
            },
        };
    }

    @Test
    @Parameters(method = "loginErrorsParams")
    public void shouldReturnAgentBankApiErrorFromLoginException(
            LoginException e, AgentBankApiError expected) {
        // given & when
        AgentBankApiError result = AuthenticationExceptionHandler.toErrorFromLoginException(e);

        // then
        assertThat(result.getClass()).isEqualTo(expected.getClass());
        assertThat(result.getDetails())
                .isEqualToIgnoringGivenFields(expected.getDetails(), "uniqueId");
    }

    private Object[] loginErrorsParams() {
        return new Object[] {
            new Object[] {
                LoginError.CREDENTIALS_VERIFICATION_ERROR.exception(), new AuthorizationError()
            },
            new Object[] {
                LoginError.INCORRECT_CREDENTIALS.exception(), new InvalidCredentialsError()
            },
            new Object[] {LoginError.DEFAULT_MESSAGE.exception(), new ThirdPartyAppUnknownError()},
            new Object[] {LoginError.NOT_CUSTOMER.exception(), new ThirdPartyAppUnknownError()},
        };
    }

    @Test
    @Parameters(method = "nemIdErrorsParams")
    public void shouldReturnAgentBankApiErrorFromNemIdException(
            NemIdException e, AgentBankApiError expected) {
        // given & when
        AgentBankApiError result = AuthenticationExceptionHandler.toErrorFromNemIdException(e);

        // then
        assertThat(result.getClass()).isEqualTo(expected.getClass());
        assertThat(result.getDetails())
                .isEqualToIgnoringGivenFields(expected.getDetails(), "uniqueId");
    }

    private Object[] nemIdErrorsParams() {
        return new Object[] {
            new Object[] {NemIdError.REJECTED.exception(), new ThirdPartyAppCancelledError()},
            new Object[] {NemIdError.TIMEOUT.exception(), new ThirdPartyAppTimedOutError()},
            new Object[] {NemIdError.CODEAPP_NOT_REGISTERED.exception(), new AuthorizationError()},
            new Object[] {NemIdError.INTERRUPTED.exception(), new ThirdPartyAppUnknownError()},
        };
    }

    @Test
    public void shouldReturnAgentBankApiErrorFromSupplementalInfoException() {
        // given
        AgentBankApiError expected = new NoUserInteractionResponseError();

        // when
        AgentBankApiError result =
                AuthenticationExceptionHandler.toErrorFromSupplementalInfoException(
                        SupplementalInfoError.WAIT_TIMEOUT.exception());

        // then
        assertThat(result.getClass()).isEqualTo(expected.getClass());
        assertThat(result.getDetails())
                .isEqualToIgnoringGivenFields(expected.getDetails(), "uniqueId");
    }

    @Test
    public void shouldThrowFromSupplementalInfoExceptionIfItIsUnknown() {
        // given & when
        Throwable throwable =
                catchThrowable(
                        () ->
                                AuthenticationExceptionHandler.toErrorFromSupplementalInfoException(
                                        SupplementalInfoError.UNKNOWN.exception()));

        // then
        assertThat(throwable)
                .isInstanceOf(SupplementalInfoException.class)
                .hasMessage("Cause: SupplementalInfoError.UNKNOWN");
    }

    @Test
    @Parameters(method = "signInFailedAuthResultParams")
    public void shouldGetProperSignInFailedAuthResult(
            boolean isAutoAuth, AgentBankApiError expectedError) {
        // given
        LunarDataAccessorFactory dataAccessorFactory =
                new LunarDataAccessorFactory(new ObjectMapperFactory().getInstance());
        LunarAuthData initialData = new LunarAuthData();
        initialData.setAccessToken("Test Token");
        LunarAuthDataAccessor lunarAuthDataAccessor =
                StepsUtils.getAuthDataAccessor(dataAccessorFactory, initialData);

        // when
        AgentFailedAuthenticationResult result =
                AuthenticationExceptionHandler.getSignInFailedAuthResult(
                        lunarAuthDataAccessor,
                        new ResponseStatusException(HttpStatus.BAD_REQUEST),
                        isAutoAuth);

        // then
        StepsUtils.assertFailedResultEquals(
                new AgentFailedAuthenticationResult(
                        expectedError, StepsUtils.getExpectedPersistedData(new LunarAuthData())),
                result);
    }

    private Object[] signInFailedAuthResultParams() {
        return new Object[] {
            new Object[] {true, new SessionExpiredError()},
            new Object[] {false, new AccessTokenFetchingFailureError()},
        };
    }
}
