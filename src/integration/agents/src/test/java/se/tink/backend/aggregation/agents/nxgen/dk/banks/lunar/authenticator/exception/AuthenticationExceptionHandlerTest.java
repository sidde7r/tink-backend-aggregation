package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.agentplatform.authentication.result.error.NoUserInteractionResponseError;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agents.exceptions.nemid.NemIdError;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.LunarTestUtils;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AuthenticationError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AuthorizationError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.InvalidCredentialsError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ServerError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ThirdPartyAppCancelledError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ThirdPartyAppNoClientError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ThirdPartyAppTimedOutError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ThirdPartyAppUnknownError;

@RunWith(JUnitParamsRunner.class)
public class AuthenticationExceptionHandlerTest {

    @Test
    @Parameters(method = "authenticationErrorsParams")
    public void shouldReturnAgentBankApiErrorFromAuthenticationException(
            AuthenticationException e, AgentBankApiError expected) {
        // given & when
        AgentBankApiError result = AuthenticationExceptionHandler.toError(e);

        // then
        assertThat(result.getClass()).isEqualTo(expected.getClass());
        assertThat(result.getDetails().getErrorCode())
                .isEqualTo(expected.getDetails().getErrorCode());
        assertThat(result.getDetails().getErrorMessage()).isEqualTo(e.getUserMessage().get());
    }

    private Object[] authenticationErrorsParams() {
        return new Object[] {
            new Object[] {
                LoginError.CREDENTIALS_VERIFICATION_ERROR.exception(), new AuthorizationError()
            },
            new Object[] {
                LoginError.INCORRECT_CREDENTIALS.exception(), new InvalidCredentialsError()
            },
            new Object[] {
                LoginError.NOT_CUSTOMER.exception(),
                new ThirdPartyAppNoClientError(
                        LunarTestUtils.getExpectedErrorDetails(
                                AgentError.INVALID_CREDENTIALS.getCode(), LoginError.NOT_CUSTOMER))
            },
            new Object[] {LoginError.DEFAULT_MESSAGE.exception(), new ThirdPartyAppUnknownError()},
            new Object[] {NemIdError.REJECTED.exception(), new ThirdPartyAppCancelledError()},
            new Object[] {
                NemIdError.INTERRUPTED.exception(),
                new AuthenticationError(
                        LunarTestUtils.getExpectedErrorDetails(
                                AgentError.THIRD_PARTY_APP_UNKNOWN_ERROR.getCode(),
                                NemIdError.INTERRUPTED))
            },
            new Object[] {
                NemIdError.NEMID_LOCKED.exception(),
                new AuthenticationError(
                        LunarTestUtils.getExpectedErrorDetails(
                                AgentError.THIRD_PARTY_APP_UNKNOWN_ERROR.getCode(),
                                NemIdError.NEMID_LOCKED))
            },
            new Object[] {
                NemIdError.NEMID_BLOCKED.exception(),
                new AuthenticationError(
                        LunarTestUtils.getExpectedErrorDetails(
                                AgentError.THIRD_PARTY_APP_UNKNOWN_ERROR.getCode(),
                                NemIdError.NEMID_BLOCKED))
            },
            new Object[] {
                NemIdError.INVALID_CODE_CARD_CODE.exception(), new InvalidCredentialsError()
            },
            new Object[] {NemIdError.USE_NEW_CODE_CARD.exception(), new InvalidCredentialsError()},
            new Object[] {
                NemIdError.INVALID_CODE_TOKEN_CODE.exception(), new InvalidCredentialsError()
            },
            new Object[] {NemIdError.TIMEOUT.exception(), new ThirdPartyAppTimedOutError()},
            new Object[] {
                NemIdError.CODE_TOKEN_NOT_SUPPORTED.exception(), new ThirdPartyAppUnknownError()
            },
            new Object[] {
                SupplementalInfoError.WAIT_TIMEOUT.exception(), new NoUserInteractionResponseError()
            },
        };
    }

    @Test
    public void shouldThrowFromSupplementalInfoExceptionIfItIsUnknown() {
        // given & when
        Throwable throwable =
                catchThrowable(
                        () ->
                                AuthenticationExceptionHandler.toError(
                                        SupplementalInfoError.UNKNOWN.exception()));

        // then
        assertThat(throwable)
                .isInstanceOf(SupplementalInfoException.class)
                .hasMessage("Cause: SupplementalInfoError.UNKNOWN");
    }

    @Test
    public void shouldReturnAgentBankApiErrorFromBankServiceException() {
        // given
        AgentBankApiError expected = new ServerError();

        // when
        AgentBankApiError result =
                AuthenticationExceptionHandler.toErrorFromBankServiceException(
                        BankServiceError.BANK_SIDE_FAILURE.exception());

        // then
        assertThat(result.getClass()).isEqualTo(expected.getClass());
        assertThat(result.getDetails().getErrorCode())
                .isEqualTo(expected.getDetails().getErrorCode());
        assertThat(result.getDetails().getErrorMessage())
                .isEqualTo(BankServiceError.BANK_SIDE_FAILURE.userMessage().get());
    }
}
