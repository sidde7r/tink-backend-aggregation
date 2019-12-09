package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.component.authenticator;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.component.authenticator.detail.Login0TestData.FAILED_REQUEST_REJECTED;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.component.authenticator.detail.Login0TestData.INCORRECT_CREDENTIALS;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.component.authenticator.detail.Login0TestData.SUCCESSFUL_LOGIN;

import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.NovoBancoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.component.authenticator.detail.Login0TestData;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class NovoBancoAuthenticatorTest {

    @Test
    public void shouldNotThrowIfLoginWithCorrectCredentials() {
        // given
        NovoBancoApiClient apiClient = mock(NovoBancoApiClient.class);
        when(apiClient.loginStep0("proper", "credentials"))
                .thenReturn(Login0TestData.getResponse(SUCCESSFUL_LOGIN));

        NovoBancoAuthenticator authenticator =
                new NovoBancoAuthenticator(apiClient, new SessionStorage());

        // when
        Throwable thrown =
                catchThrowable(
                        () -> {
                            authenticator.authenticate("proper", "credentials");
                        });

        // then
        assertNull(thrown);
    }

    @Test(expected = LoginException.class)
    public void shouldThrowIfLoginWithWrongCredentials()
            throws AuthenticationException, AuthorizationException {
        // given
        NovoBancoApiClient apiClient = mock(NovoBancoApiClient.class);
        when(apiClient.loginStep0("wrong", "credentials"))
                .thenReturn(Login0TestData.getResponse(INCORRECT_CREDENTIALS));

        NovoBancoAuthenticator authenticator =
                new NovoBancoAuthenticator(apiClient, new SessionStorage());
        // when
        authenticator.authenticate("wrong", "credentials");
    }

    @Test(expected = LoginException.class)
    public void shouldThrowIfLoginWithInvalidRequest()
            throws AuthenticationException, AuthorizationException {
        // given
        NovoBancoApiClient apiClient = mock(NovoBancoApiClient.class);
        // bank answers like below when e.g. when encryption is incorrect (or digest does not match)
        when(apiClient.loginStep0("wrong", "request"))
                .thenReturn(Login0TestData.getResponse(FAILED_REQUEST_REJECTED));

        NovoBancoAuthenticator authenticator =
                new NovoBancoAuthenticator(apiClient, new SessionStorage());
        // when
        authenticator.authenticate("wrong", "request");
    }
}
