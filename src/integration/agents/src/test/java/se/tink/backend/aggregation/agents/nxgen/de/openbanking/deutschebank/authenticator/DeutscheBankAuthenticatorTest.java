package se.tink.backend.aggregation.agents.nxgen.de.openbanking.deutschebank.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.DeutscheBankAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class DeutscheBankAuthenticatorTest {

    private DeutscheBankApiClient deutscheBankApiClient;
    private DeutscheBankAuthenticator deutscheBankAuthenticator;

    @Before
    public void setup() {
        deutscheBankApiClient = mock(DeutscheBankApiClient.class);
        SessionStorage sessionStorage = mock(SessionStorage.class);
        deutscheBankAuthenticator =
                new DeutscheBankAuthenticator(deutscheBankApiClient, sessionStorage, "", "");
    }

    @Test
    public void shouldConfirmValidStatus() {
        // Given
        ConsentStatusResponse expected =
                SerializationUtils.deserializeFromString(
                        "{\"consentStatus\": \"valid\"}", ConsentStatusResponse.class);
        when(deutscheBankApiClient.getConsentStatus()).thenReturn(expected);

        // When
        Throwable t = catchThrowable(deutscheBankAuthenticator::confirmAuthentication);

        // Then
        assertThat(t).doesNotThrowAnyException();
    }

    @Test
    public void shouldThrowLoginErrorWhenExpiredStatus() {
        // Given
        ConsentStatusResponse expected =
                SerializationUtils.deserializeFromString(
                        "{\"consentStatus\": \"expired\"}", ConsentStatusResponse.class);
        when(deutscheBankApiClient.getConsentStatus()).thenReturn(expected);

        // When
        Throwable t = catchThrowable(deutscheBankAuthenticator::confirmAuthentication);

        // Then
        assertThat(t)
                .isInstanceOf(LoginException.class)
                .hasMessage(LoginError.CREDENTIALS_VERIFICATION_ERROR.exception().getMessage());
    }

    @Test
    public void shouldThrowLoginErrorWhenStatusReceived() {
        // Given
        ConsentStatusResponse expected =
                SerializationUtils.deserializeFromString(
                        "{\"consentStatus\": \"received\"}", ConsentStatusResponse.class);
        when(deutscheBankApiClient.getConsentStatus()).thenReturn(expected);

        // When
        Throwable t = catchThrowable(deutscheBankAuthenticator::confirmAuthentication);

        // Then
        assertThat(t)
                .isInstanceOf(LoginException.class)
                .hasMessage(LoginError.CREDENTIALS_VERIFICATION_ERROR.exception().getMessage());
    }

    @Test
    public void shouldThrowLoginErrorWhenErrorMessage() {
        // Given
        ConsentStatusResponse expected =
                SerializationUtils.deserializeFromString(
                        "{\"tppMessages\":[{\"code\":\"CONSENT_INVALID\",\"text\":\"Test.\",\"category\":\"ERROR\"}],\"transactionStatus\":\"RJCT\"}\n",
                        ConsentStatusResponse.class);
        when(deutscheBankApiClient.getConsentStatus()).thenReturn(expected);

        // When
        Throwable t = catchThrowable(deutscheBankAuthenticator::confirmAuthentication);

        // Then
        assertThat(t)
                .isInstanceOf(LoginException.class)
                .hasMessage(LoginError.CREDENTIALS_VERIFICATION_ERROR.exception().getMessage());
    }

    @Test
    public void shouldThrowLoginErrorWhenStatusResponseIsNull() {
        // Given
        when(deutscheBankApiClient.getConsentStatus()).thenReturn(null);

        // When
        Throwable t = catchThrowable(deutscheBankAuthenticator::confirmAuthentication);

        // Then
        assertThat(t)
                .isInstanceOf(LoginException.class)
                .hasMessage(LoginError.CREDENTIALS_VERIFICATION_ERROR.exception().getMessage());
    }

    @Test
    public void shouldThrowLoginErrorWhenStatusResponseIsEmpty() {
        // Given
        ConsentStatusResponse expected =
                SerializationUtils.deserializeFromString("", ConsentStatusResponse.class);
        when(deutscheBankApiClient.getConsentStatus()).thenReturn(expected);

        // When
        Throwable t = catchThrowable(deutscheBankAuthenticator::confirmAuthentication);

        // Then
        assertThat(t)
                .isInstanceOf(LoginException.class)
                .hasMessage(LoginError.CREDENTIALS_VERIFICATION_ERROR.exception().getMessage());
    }
}
