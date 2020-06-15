package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaTestFixtures.createAuthenticationRequest;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.steps.data.GetLoginDetailsStatus;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.steps.helpers.AktiaAccessTokenRetriever;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.steps.helpers.AktiaLoginDetailsFetcher;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;

public class LoginStepTest {

    private LoginStep loginStep;

    private AktiaLoginDetailsFetcher loginDetailsFetcherMock;

    @Before
    public void setUp() {
        final AktiaAccessTokenRetriever accessTokenRetrieverMock =
                mock(AktiaAccessTokenRetriever.class);
        loginDetailsFetcherMock = mock(AktiaLoginDetailsFetcher.class);

        loginStep = new LoginStep(accessTokenRetrieverMock, loginDetailsFetcherMock);
    }

    @Test
    public void shouldReturnSucceedIfLoggedIn()
            throws AuthenticationException, AuthorizationException {
        // given
        final AuthenticationRequest authenticationRequest = createAuthenticationRequest();

        when(loginDetailsFetcherMock.getLoginDetails()).thenReturn(GetLoginDetailsStatus.LOGGED_IN);

        // when
        final AuthenticationStepResponse returnedResponse =
                loginStep.execute(authenticationRequest);

        // then
        assertThat(returnedResponse.getNextStepId().isPresent()).isFalse();
        assertThat(returnedResponse.isAuthenticationFinished()).isTrue();
    }

    @Test
    public void shouldReturnExecuteNextStepIfOtpIsRequired()
            throws AuthenticationException, AuthorizationException {
        // given
        final AuthenticationRequest authenticationRequest = createAuthenticationRequest();

        when(loginDetailsFetcherMock.getLoginDetails())
                .thenReturn(GetLoginDetailsStatus.OTP_REQUIRED);

        // when
        final AuthenticationStepResponse returnedResponse =
                loginStep.execute(authenticationRequest);

        // then
        assertThat(returnedResponse).isEqualTo(AuthenticationStepResponse.executeNextStep());
    }

    @Test
    public void shouldThrowExceptionForResponseHasError() {
        // given
        final AuthenticationRequest authenticationRequest = createAuthenticationRequest();

        when(loginDetailsFetcherMock.getLoginDetails())
                .thenReturn(GetLoginDetailsStatus.ERROR_IN_RESPONSE);

        // when
        final Throwable thrown = catchThrowable(() -> loginStep.execute(authenticationRequest));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Error in response from server.");
    }

    @Test
    public void shouldThrowExceptionIfPasswordChangeRequired() {
        // given
        final AuthenticationRequest authenticationRequest = createAuthenticationRequest();

        when(loginDetailsFetcherMock.getLoginDetails())
                .thenReturn(GetLoginDetailsStatus.PASSWORD_CHANGE_REQUIRED);

        // when
        final Throwable thrown = catchThrowable(() -> loginStep.execute(authenticationRequest));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(SessionException.class)
                .hasMessage("Cause: SessionError.SESSION_EXPIRED");
    }

    @Test
    public void shouldThrowExceptionIfAccountIsLocked() {
        // given
        final AuthenticationRequest authenticationRequest = createAuthenticationRequest();

        when(loginDetailsFetcherMock.getLoginDetails())
                .thenReturn(GetLoginDetailsStatus.ACCOUNT_LOCKED);

        // when
        final Throwable thrown = catchThrowable(() -> loginStep.execute(authenticationRequest));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(AuthorizationException.class)
                .hasMessage("Cause: AuthorizationError.ACCOUNT_BLOCKED");
    }

    @Test
    public void shouldThrowExceptionIfUserMustAcceptTerms() {
        // given
        final AuthenticationRequest authenticationRequest = createAuthenticationRequest();

        when(loginDetailsFetcherMock.getLoginDetails())
                .thenReturn(GetLoginDetailsStatus.MUST_ACCEPT_TERMS);

        // when
        final Throwable thrown = catchThrowable(() -> loginStep.execute(authenticationRequest));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(SessionException.class)
                .hasMessage("Cause: SessionError.SESSION_EXPIRED");
    }
}
