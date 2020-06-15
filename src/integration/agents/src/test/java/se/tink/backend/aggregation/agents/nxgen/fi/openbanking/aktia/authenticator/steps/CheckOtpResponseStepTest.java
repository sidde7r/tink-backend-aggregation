package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaTestFixtures.createAuthenticationRequest;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.AktiaOtpDataStorage;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.steps.data.ExchangeOtpCodeStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;

public class CheckOtpResponseStepTest {

    private CheckOtpResponseStep checkOtpResponseStep;

    private AktiaOtpDataStorage otpDataStorageMock;

    @Before
    public void setUp() {
        otpDataStorageMock = mock(AktiaOtpDataStorage.class);

        checkOtpResponseStep = new CheckOtpResponseStep(otpDataStorageMock);
    }

    @Test
    public void shouldReturnSucceedForAcceptedOtpCode()
            throws AuthenticationException, AuthorizationException {
        // given
        final AuthenticationRequest authenticationRequest = createAuthenticationRequest();

        when(otpDataStorageMock.getStatus())
                .thenReturn(Optional.of(ExchangeOtpCodeStatus.ACCEPTED));

        // when
        final AuthenticationStepResponse returnedResponse =
                checkOtpResponseStep.execute(authenticationRequest);

        // then
        assertThat(returnedResponse.getNextStepId().isPresent()).isFalse();
        assertThat(returnedResponse.isAuthenticationFinished()).isTrue();
        assertThat(returnedResponse.getSupplementInformationRequester().isPresent()).isFalse();
    }

    @Test
    public void shouldReturnExecuteNextStepForWrongOtpCode()
            throws AuthenticationException, AuthorizationException {
        // given
        final AuthenticationRequest authenticationRequest = createAuthenticationRequest();

        when(otpDataStorageMock.getStatus())
                .thenReturn(Optional.of(ExchangeOtpCodeStatus.WRONG_OTP_CODE));

        // when
        final AuthenticationStepResponse returnedResponse =
                checkOtpResponseStep.execute(authenticationRequest);

        // then
        assertThat(returnedResponse.getNextStepId().isPresent()).isTrue();
        returnedResponse
                .getNextStepId()
                .ifPresent(stepId -> assertThat(stepId).isEqualTo(AuthorizeWithOtpStep.STEP_ID));
        assertThat(returnedResponse.isAuthenticationFinished()).isFalse();
        assertThat(returnedResponse.getSupplementInformationRequester().isPresent()).isFalse();
    }

    @Test
    public void shouldThrowExceptionForResponseHasError() {
        // given
        final AuthenticationRequest authenticationRequest = createAuthenticationRequest();

        when(otpDataStorageMock.getStatus())
                .thenReturn(Optional.of(ExchangeOtpCodeStatus.OTHER_ERROR));

        // when
        final Throwable thrown =
                catchThrowable(() -> checkOtpResponseStep.execute(authenticationRequest));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(SessionException.class)
                .hasMessage("Cause: SessionError.SESSION_EXPIRED");
    }

    @Test
    public void shouldThrowExceptionForLockedAccount() {
        // given
        final AuthenticationRequest authenticationRequest = createAuthenticationRequest();

        when(otpDataStorageMock.getStatus())
                .thenReturn(Optional.of(ExchangeOtpCodeStatus.ACCOUNT_LOCKED));

        // when
        final Throwable thrown =
                catchThrowable(() -> checkOtpResponseStep.execute(authenticationRequest));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(AuthorizationException.class)
                .hasMessage("Cause: AuthorizationError.ACCOUNT_BLOCKED");
    }

    @Test
    public void shouldThrowExceptionWhenExchangeOtpCodeStatusIsNotInTheStorage() {
        // given
        final AuthenticationRequest authenticationRequest = createAuthenticationRequest();

        when(otpDataStorageMock.getStatus()).thenReturn(Optional.empty());

        // when
        final Throwable thrown =
                catchThrowable(() -> checkOtpResponseStep.execute(authenticationRequest));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Exchange OTP code status not found in the storage.");
    }
}
