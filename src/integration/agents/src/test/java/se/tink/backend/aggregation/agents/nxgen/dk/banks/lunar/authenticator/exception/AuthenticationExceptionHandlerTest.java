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
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AuthorizationError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.InvalidCredentialsError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ThirdPartyAppCancelledError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ThirdPartyAppTimedOutError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ThirdPartyAppUnknownError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception.NemIdError;

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
        assertThat(result.getDetails())
                .isEqualToIgnoringGivenFields(expected.getDetails(), "uniqueId");
    }

    private Object[] authenticationErrorsParams() {
        return new Object[] {
            new Object[] {
                LoginError.CREDENTIALS_VERIFICATION_ERROR.exception(), new AuthorizationError()
            },
            new Object[] {
                LoginError.INCORRECT_CREDENTIALS.exception(), new InvalidCredentialsError()
            },
            new Object[] {LoginError.DEFAULT_MESSAGE.exception(), new ThirdPartyAppUnknownError()},
            new Object[] {LoginError.NOT_CUSTOMER.exception(), new ThirdPartyAppUnknownError()},
            new Object[] {NemIdError.REJECTED.exception(), new ThirdPartyAppCancelledError()},
            new Object[] {NemIdError.TIMEOUT.exception(), new ThirdPartyAppTimedOutError()},
            new Object[] {NemIdError.CODEAPP_NOT_REGISTERED.exception(), new AuthorizationError()},
            new Object[] {NemIdError.INTERRUPTED.exception(), new ThirdPartyAppUnknownError()},
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
}
