package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.exception;

import static org.assertj.core.api.Assertions.assertThat;

import agents_platform_framework.org.springframework.http.HttpStatus;
import agents_platform_framework.org.springframework.web.server.ResponseStatusException;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.agentplatform.authentication.ObjectMapperFactory;
import se.tink.backend.aggregation.agents.agentplatform.authentication.result.error.LastAttemptError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.LunarTestUtils;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarAuthData;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarAuthDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarDataAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AccessTokenFetchingFailureError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AccountBlockedError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AuthorizationError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.InvalidCredentialsError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ServerError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.SessionExpiredError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ThirdPartyAppNoClientError;

@RunWith(JUnitParamsRunner.class)
public class LunarAuthExceptionHandlerTest {

    @Test
    @Parameters(method = "knownErrorsParams")
    public void shouldMapToKnownErrorFromResponseOrDefault(
            ResponseStatusException e, AgentBankApiError expected) {
        // given & when
        AgentBankApiError result =
                LunarAuthExceptionHandler.toKnownErrorFromResponseOrDefault(
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
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "{}"), new AuthorizationError()
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
                        "{\"reasonCode\": \"USER_PASSWORD_INCORRECT_RESET_APP\"}"),
                new InvalidCredentialsError(LastAttemptError.getError())
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
                new ThirdPartyAppNoClientError(
                        LunarTestUtils.getExpectedErrorDetails(
                                AgentError.THIRD_PARTY_APP_NO_CLIENT.getCode(),
                                LoginError.NOT_CUSTOMER))
            },
            new Object[] {
                new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "{\"reasonCode\": \"ACCESS_TOKEN_REVOKED_BY_CUSTOMER_SUPPORT\"}"),
                new AccountBlockedError(
                        LunarTestUtils.getExpectedErrorDetails(
                                AgentError.ACCOUNT_BLOCKED.getCode(),
                                LoginError.INVALIDATED_CREDENTIALS))
            },
            new Object[] {
                new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR),
                new ServerError(
                        LunarTestUtils.getExpectedErrorDetails(
                                AgentError.HTTP_RESPONSE_ERROR.getCode(),
                                BankServiceError.BANK_SIDE_FAILURE))
            },
            new Object[] {
                new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED), new ServerError()
            },
            new Object[] {
                new ResponseStatusException(HttpStatus.BAD_GATEWAY),
                new ServerError(
                        LunarTestUtils.getExpectedErrorDetails(
                                AgentError.HTTP_RESPONSE_ERROR.getCode(),
                                BankServiceError.NO_BANK_SERVICE))
            },
            new Object[] {
                new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE),
                new ServerError(
                        LunarTestUtils.getExpectedErrorDetails(
                                AgentError.HTTP_RESPONSE_ERROR.getCode(),
                                BankServiceError.NO_BANK_SERVICE))
            },
        };
    }

    @Test
    @Parameters(method = "signInFailedAuthResultParams")
    public void shouldGetProperSignInFailedAuthResult(
            boolean isAutoAuth, AgentBankApiError expectedError) {
        // given
        LunarDataAccessorFactory dataAccessorFactory =
                new LunarDataAccessorFactory(new ObjectMapperFactory().getInstance());
        LunarAuthData authData = new LunarAuthData();
        LunarAuthDataAccessor authDataAccessor =
                LunarTestUtils.getAuthDataAccessor(dataAccessorFactory, authData);

        // & when
        AgentFailedAuthenticationResult result =
                LunarAuthExceptionHandler.getSignInFailedAuthResult(
                        authDataAccessor,
                        new ResponseStatusException(HttpStatus.BAD_REQUEST),
                        isAutoAuth);

        // then
        LunarTestUtils.assertFailedResultEquals(
                new AgentFailedAuthenticationResult(
                        expectedError, LunarTestUtils.toPersistedData(new LunarAuthData())),
                result);
    }

    private Object[] signInFailedAuthResultParams() {
        return new Object[] {
            new Object[] {true, new SessionExpiredError()},
            new Object[] {false, new AccessTokenFetchingFailureError()},
        };
    }
}
