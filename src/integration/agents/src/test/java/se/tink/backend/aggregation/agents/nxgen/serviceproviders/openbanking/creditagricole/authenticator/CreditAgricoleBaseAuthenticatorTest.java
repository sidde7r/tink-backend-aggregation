package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.authenticator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.configuration.CreditAgricoleBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.apiclient.CreditAgricoleBaseApiClient;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class CreditAgricoleBaseAuthenticatorTest {

    private CreditAgricoleBaseApiClient apiClient;
    private PersistentStorage persistentStorage;
    private AgentConfiguration<CreditAgricoleBaseConfiguration> agentConfiguration;
    private CreditAgricoleBaseConfiguration configuration;

    private CreditAgricoleBaseAuthenticator creditAgricoleBaseAuthenticator;

    @Before
    public void init() {
        apiClient = mock(CreditAgricoleBaseApiClient.class);
        persistentStorage = mock(PersistentStorage.class);
        configuration = mock(CreditAgricoleBaseConfiguration.class);
        String clientId = "clientId";
        String redirectUrl = "redirectUrl";
        String authorizeUrl = "authorizeUrl";
        agentConfiguration =
                new AgentConfiguration.Builder<>()
                        .setProviderSpecificConfiguration(configuration)
                        .setRedirectUrl(redirectUrl)
                        .build();
        when(configuration.getClientId()).thenReturn(clientId);
        when(configuration.getAuthorizeUrl()).thenReturn(authorizeUrl);

        creditAgricoleBaseAuthenticator =
                new CreditAgricoleBaseAuthenticator(
                        apiClient, persistentStorage, agentConfiguration);
    }

    @Test
    public void shouldBuildAuthorizeUrl() {
        // given
        String state = "state";
        String expectedUrl =
                "authorizeUrl?client_id=clientId&response_type=code&scope=aisp+extended_transaction_history&redirect_uri=redirectUrl&state=state";

        // when
        URL url = creditAgricoleBaseAuthenticator.buildAuthorizeUrl(state);

        // then
        assertNotNull(url);
        assertEquals(expectedUrl, url.get());
    }

    @Test
    public void shouldExchangeAuthorizationCode() throws AuthenticationException {
        // given
        String code = "code";
        TokenResponse tokenResponse = mock(TokenResponse.class);
        OAuth2Token oAuth2Token = mock(OAuth2Token.class);

        when(tokenResponse.toTinkToken()).thenReturn(oAuth2Token);
        when(apiClient.getToken(anyString())).thenReturn(tokenResponse);

        // when
        OAuth2Token resp = creditAgricoleBaseAuthenticator.exchangeAuthorizationCode(code);

        // then
        assertEquals(oAuth2Token, resp);
    }

    @Test
    public void shouldRefreshToken() throws AuthenticationException {
        // given
        String refreshToken = "refreshToken";
        OAuth2Token oAuth2Token = mock(OAuth2Token.class);

        when(apiClient.refreshToken(anyString())).thenReturn(oAuth2Token);

        // when
        OAuth2Token resp = creditAgricoleBaseAuthenticator.refreshAccessToken(refreshToken);

        // then
        assertEquals(oAuth2Token, resp);
    }

    @Test
    public void shouldUseAccessToken() {
        // given
        OAuth2Token accessToken = mock(OAuth2Token.class);

        when(persistentStorage.put(
                        CreditAgricoleBaseConstants.StorageKeys.OAUTH_TOKEN, accessToken))
                .thenReturn("");

        // when
        creditAgricoleBaseAuthenticator.useAccessToken(accessToken);

        // then
        verify(persistentStorage)
                .put(CreditAgricoleBaseConstants.StorageKeys.OAUTH_TOKEN, accessToken);
    }
}
