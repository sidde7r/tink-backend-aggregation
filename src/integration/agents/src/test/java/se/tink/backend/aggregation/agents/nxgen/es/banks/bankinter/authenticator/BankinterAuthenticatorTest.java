package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.authenticator;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openqa.selenium.TimeoutException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.LoginForm;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.ScaForm;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;

@RunWith(MockitoJUnitRunner.class)
public class BankinterAuthenticatorTest {
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    @Mock private SupplementalInformationHelper supplementalInformationHelper;
    @Mock private BankinterAuthenticationClient authenticationClient;
    private BankinterAuthenticator authenticator;

    @Before
    public void setUp() throws Exception {
        this.authenticator =
                new BankinterAuthenticator(supplementalInformationHelper, authenticationClient);
    }

    @Test
    public void shouldAuthenticateUserProperlyWithoutSCA() {
        // given
        String loginUrl = "loginUrl";
        when(authenticationClient.login(USERNAME, PASSWORD)).thenReturn(loginUrl);
        when(authenticationClient.isScaNeeded()).thenReturn(false);

        // when
        authenticator.authenticate(USERNAME, PASSWORD);

        // then
        verify(authenticationClient, times(1)).login(USERNAME, PASSWORD);
        verify(authenticationClient, times(1)).finishProcess();
        verify(authenticationClient, times(0)).submitSca(supplementalInformationHelper);
        verifyZeroInteractions(supplementalInformationHelper);
    }

    @Test
    public void shouldAuthenticateUserProperlyWithSCA() {
        // given
        String loginUrl = "loginUrl";
        when(authenticationClient.login(USERNAME, PASSWORD)).thenReturn(loginUrl);
        when(authenticationClient.isScaNeeded()).thenReturn(true);
        String scaUrl = "scaUrl";
        when(authenticationClient.submitSca(supplementalInformationHelper)).thenReturn(scaUrl);

        // when
        authenticator.authenticate(USERNAME, PASSWORD);

        // then
        verify(authenticationClient, times(1)).login(USERNAME, PASSWORD);
        verify(authenticationClient, times(1)).finishProcess();
        verify(authenticationClient, times(1)).submitSca(supplementalInformationHelper);
    }

    @Test
    public void shouldThrowTimeoutExceptionWhenTimeFOrLoginFormHasExceeded() {
        // given
        String loginUrl = "loginUrl";
        when(authenticationClient.login(USERNAME, PASSWORD)).thenReturn(loginUrl);
        doThrow(TimeoutException.class)
                .when(authenticationClient)
                .waitForErrorOrRedirect(LoginForm.SUBMIT_TIMEOUT_SECONDS, loginUrl);

        // when
        ThrowingCallable result = () -> authenticator.authenticate(USERNAME, PASSWORD);

        // then
        assertThatThrownBy(result).isInstanceOf(TimeoutException.class);
    }

    @Test
    public void shouldThrowTimeoutExceptionWhenTimeForSCAFormHasExceeded() {
        // given
        String loginUrl = "loginUrl";
        when(authenticationClient.login(USERNAME, PASSWORD)).thenReturn(loginUrl);
        when(authenticationClient.isScaNeeded()).thenReturn(true);
        String scaUrl = "scaUrl";
        when(authenticationClient.submitSca(supplementalInformationHelper)).thenReturn(scaUrl);
        doThrow(TimeoutException.class)
                .when(authenticationClient)
                .waitForErrorOrRedirect(ScaForm.SUBMIT_TIMEOUT_SECONDS, scaUrl);

        // when
        ThrowingCallable result = () -> authenticator.authenticate(USERNAME, PASSWORD);

        // then
        assertThatThrownBy(result).isInstanceOf(TimeoutException.class);
    }
}
