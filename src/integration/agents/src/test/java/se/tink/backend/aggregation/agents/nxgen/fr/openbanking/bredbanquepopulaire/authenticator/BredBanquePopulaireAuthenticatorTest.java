package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.BredBanquePopulaireTestFixtures.ACCESS_TOKEN;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.BredBanquePopulaireTestFixtures.EXCHANGE_CODE;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.BredBanquePopulaireTestFixtures.OAUTH_URL;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.BredBanquePopulaireTestFixtures.REFRESH_TOKEN;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.BredBanquePopulaireTestFixtures.STATE;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.BredBanquePopulaireTestFixtures.TOKEN_EXPIRES_IN;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.BredBanquePopulaireTestFixtures.TOKEN_TYPE;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.apiclient.BredBanquePopulaireApiClient;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RunWith(MockitoJUnitRunner.class)
public class BredBanquePopulaireAuthenticatorTest {
    private static final URL AUTHORIZE_URL = new URL(String.format(OAUTH_URL, STATE));

    @Mock private BredBanquePopulaireApiClient apiClient;
    @Mock private PersistentStorage persistentStorage;
    private BredBanquePopulaireAuthenticator bredBanquePopulaireAuthenticator;

    @Before
    public void setUp() {
        final OAuth2Token token =
                OAuth2Token.create(TOKEN_TYPE, ACCESS_TOKEN, REFRESH_TOKEN, TOKEN_EXPIRES_IN);

        when(apiClient.getAuthorizeUrl(STATE)).thenReturn(AUTHORIZE_URL);
        when(apiClient.exchangeAuthorizationToken(EXCHANGE_CODE)).thenReturn(token);
        when(apiClient.exchangeRefreshToken(EXCHANGE_CODE)).thenReturn(token);

        bredBanquePopulaireAuthenticator =
                new BredBanquePopulaireAuthenticator(apiClient, persistentStorage);
    }

    @Test
    public void shouldReturnURLWithState() {
        // when
        final URL builtURL = bredBanquePopulaireAuthenticator.buildAuthorizeUrl(STATE);

        // then
        assertTrue(builtURL.get().contains(STATE));
    }

    @Test
    public void shouldExchangeAuthorizationCode() {
        // when
        final OAuth2Token returnedToken =
                bredBanquePopulaireAuthenticator.exchangeAuthorizationCode(EXCHANGE_CODE);

        // then
        assertTokenIsValid(returnedToken);
    }

    @Test
    public void shouldThrowExceptionWhenHttpResponseExceptionOccurs() {
        // given
        final HttpRequest httpRequest = mock(HttpRequest.class);
        final HttpResponse httpResponse = mock(HttpResponse.class);
        when(apiClient.exchangeAuthorizationToken(anyString()))
                .thenThrow(new HttpResponseException(httpRequest, httpResponse));

        // when
        final Throwable thrown =
                catchThrowable(
                        () ->
                                bredBanquePopulaireAuthenticator.exchangeAuthorizationCode(
                                        EXCHANGE_CODE));

        // then
        assertThat(thrown).isInstanceOf(HttpResponseException.class);
    }

    @Test
    public void shouldRefreshAccessToken() throws SessionException {
        // when
        final OAuth2Token returnedToken =
                bredBanquePopulaireAuthenticator.refreshAccessToken(EXCHANGE_CODE);

        // then
        assertTokenIsValid(returnedToken);
    }

    private static void assertTokenIsValid(OAuth2Token token) {
        assertNotNull(token);
        assertTrue(token.isValid());
        assertFalse(token.isRefreshTokenExpirationPeriodSpecified());
        assertTrue(token.canRefresh());
        assertThat(token.getAccessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(token.getTokenType()).isEqualTo(TOKEN_TYPE);
    }
}
