package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.StorageKeys.*;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.KbcAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupApiClient;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class KbcAuthenticatorTest {
    private BerlinGroupApiClient apiClient;
    private KbcAuthenticator authenticator;
    private OAuth2Token token = mock(OAuth2Token.class);

    @Before
    public void init() {
        apiClient = mock(BerlinGroupApiClient.class);
        authenticator = new KbcAuthenticator(apiClient);
    }

    @Test
    public void shouldRefreshAccessTokenAndConvertItToTinkModel() {
        // given
        String refreshToken = "REFRESH_TOKEN";
        when(apiClient.refreshToken(any())).thenReturn(token);

        // when
        OAuth2Token result = authenticator.refreshAccessToken(refreshToken);

        // then
        assertThat(result).isEqualTo(token);
        verify(apiClient).refreshToken(refreshToken);
        verify(apiClient).setTokenToSession(token, OAUTH_TOKEN);
        verifyNoMoreInteractions(apiClient);
    }

    @Test
    public void shouldStoreAccessTokenInTheStorage() {
        // given
        // when
        authenticator.useAccessToken(token);

        // then
        verify(apiClient).setTokenToSession(token, OAUTH_TOKEN);
        verifyNoMoreInteractions(apiClient);
    }

    @Test
    public void shouldExchangeAuthorizationCodeAndConvertItToTinkModel() {
        // given
        String code = "CODE";
        when(apiClient.getToken(any())).thenReturn(token);

        // when
        OAuth2Token result = authenticator.exchangeAuthorizationCode(code);

        // then
        assertThat(result).isEqualTo(token);
        verify(apiClient).getToken(code);
        verifyNoMoreInteractions(apiClient);
    }

    @Test
    public void shouldBuildAuthorizeUrl() {
        // given
        String state = "STATE";
        when(apiClient.getAuthorizeUrl(state)).thenReturn(new URL("http://mock.com"));

        // when
        URL authorizeUrl = authenticator.buildAuthorizeUrl(state);

        // then
        assertThat(authorizeUrl.toString()).isEqualTo("http://mock.com");
        verify(apiClient).getAuthorizeUrl(state);
        verifyNoMoreInteractions(apiClient);
    }
}
