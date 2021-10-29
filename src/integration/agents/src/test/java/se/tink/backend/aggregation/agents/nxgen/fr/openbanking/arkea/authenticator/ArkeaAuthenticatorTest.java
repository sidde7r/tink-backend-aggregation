package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.authenticator;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.fetcher.ArkeaFetcherFixtures.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.apiclient.ArkeaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.configuration.ArkeaConfiguration;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RunWith(MockitoJUnitRunner.class)
public class ArkeaAuthenticatorTest {

    private PersistentStorage persistentStorage;
    private ArkeaAuthenticator arkeaAuthenticator;

    @Mock private ArkeaApiClient apiClient;
    @Mock private AgentConfiguration<ArkeaConfiguration> agentConfiguration;
    @Mock private ArkeaConfiguration arkeaConfiguration;

    @Before
    public void setUp() {
        String clientId = "clientId";
        String redirectUrl = "https://api.tink.com/api/v1/credentials/third-party/callback";
        persistentStorage = new PersistentStorage();

        when(agentConfiguration.getProviderSpecificConfiguration()).thenReturn(arkeaConfiguration);
        when(agentConfiguration.getRedirectUrl()).thenReturn(redirectUrl);
        when(arkeaConfiguration.getClientKey()).thenReturn(clientId);
        when(apiClient.exchangeAuthorizationCode(any())).thenReturn(TOKEN_RESPONSE);
        when(apiClient.refreshAccessToken(any())).thenReturn(TOKEN_RESPONSE);

        arkeaAuthenticator =
                new ArkeaAuthenticator(apiClient, persistentStorage, agentConfiguration);
    }

    @Test
    public void shouldBuildAuthorizeUrl() {
        // given
        URL expectedUrl =
                new URL(
                        "https://openbanking.cmb.fr/authorize?"
                                + "response_type=code&"
                                + "client_id=clientId&"
                                + "redirect_uri=https://api.tink.com/api/v1/credentials/third-party/callback&"
                                + "scope=aisp&"
                                + "state=state");

        // when
        String state = "state";
        URL authorizeUrl = arkeaAuthenticator.buildAuthorizeUrl(state);

        // then
        assertThat(authorizeUrl.getUrl()).isEqualTo(expectedUrl.getUrl());
    }

    @Test
    public void shouldExchangeAuthorizationCode() {
        // given
        final OAuth2Token expectedToken =
                OAuth2Token.create("Bearer", "accessToken", "refreshToken", 3599);

        // when
        String code = "code";
        final OAuth2Token returnedToken = arkeaAuthenticator.exchangeAuthorizationCode(code);

        // then
        assertThat(returnedToken).usingRecursiveComparison().isEqualTo(expectedToken);
        assertTrue(returnedToken.isValid());
    }

    @Test
    public void shouldRefreshAccessToken() {
        // given
        final OAuth2Token expectedToken =
                OAuth2Token.create("Bearer", "accessToken", "refreshToken", 3599);

        // when
        String refreshToken = "refreshToken";
        final OAuth2Token returnedToken = arkeaAuthenticator.refreshAccessToken(refreshToken);

        // then
        assertThat(returnedToken).usingRecursiveComparison().isEqualTo(expectedToken);
        assertTrue(returnedToken.isValid());
    }

    @Test
    public void shouldPutAccessTokenInPersistentStorage() {
        // given
        OAuth2Token tokenToPutInPersistentStorage =
                OAuth2Token.create("Bearer", "accessToken", "refreshToken", 3599);

        // when
        arkeaAuthenticator.useAccessToken(tokenToPutInPersistentStorage);
        String oauthToken = "OAUTH_TOKEN";
        OAuth2Token tokenFromPersistentStorage =
                persistentStorage.get(oauthToken, OAuth2Token.class).get();

        // then
        assertThat(tokenFromPersistentStorage)
                .usingRecursiveComparison()
                .isEqualTo(tokenToPutInPersistentStorage);
    }
}
