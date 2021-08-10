package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.authenticator;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openqa.selenium.TimeoutException;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.authenticator.page.AttemptsLimitExceededException;
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
    public void shouldAuthenticateUserProperlyWithoutSCA() throws AttemptsLimitExceededException {
        // given
        when(authenticationClient.isScaNeeded()).thenReturn(false);

        // when
        authenticator.authenticate(USERNAME, PASSWORD);

        // then
        verify(authenticationClient, times(1)).login(USERNAME, PASSWORD);
        verify(authenticationClient, times(1)).finishProcess();
        verify(authenticationClient, times(0)).submitSca(supplementalInformationHelper);
        verifyNoMoreInteractions(supplementalInformationHelper);
    }

    @Test
    public void shouldAuthenticateUserProperlyWithSCA() throws AttemptsLimitExceededException {
        // given
        when(authenticationClient.isScaNeeded()).thenReturn(true);

        // when
        authenticator.authenticate(USERNAME, PASSWORD);

        // then
        verify(authenticationClient, times(1)).login(USERNAME, PASSWORD);
        verify(authenticationClient, times(1)).finishProcess();
        verify(authenticationClient, times(1)).submitSca(supplementalInformationHelper);
    }

    @Test
    public void shouldThrowThirdPartyAppExceptionWithTimeoutWhenTimeFOrLoginFormHasExceeded()
            throws AttemptsLimitExceededException {
        // given
        doThrow(TimeoutException.class).when(authenticationClient).login(USERNAME, PASSWORD);

        // when
        ThrowingCallable result = () -> authenticator.authenticate(USERNAME, PASSWORD);

        // then
        assertThatThrownBy(result).isInstanceOf(ThirdPartyAppException.class);
    }

    @Test
    public void shouldThrowThirdPartyAppExceptionWithTimeoutWhenTimeForSCAFormHasExceeded()
            throws AttemptsLimitExceededException {
        // given
        when(authenticationClient.isScaNeeded()).thenReturn(true);
        doThrow(TimeoutException.class)
                .when(authenticationClient)
                .submitSca(supplementalInformationHelper);

        // when
        ThrowingCallable result = () -> authenticator.authenticate(USERNAME, PASSWORD);

        // then
        assertThatThrownBy(result).isInstanceOf(ThirdPartyAppException.class);
    }

    @Test
    public void shouldThrowThirdPartyAppExceptionWithAuthenticationErrorWhenAttemptsLimitExceeded()
            throws AttemptsLimitExceededException {
        // given
        doThrow(AttemptsLimitExceededException.class)
                .when(authenticationClient)
                .login(USERNAME, PASSWORD);

        // when
        ThrowingCallable result = () -> authenticator.authenticate(USERNAME, PASSWORD);

        // then
        assertThatThrownBy(result).isInstanceOf(ThirdPartyAppException.class);
    }
}
