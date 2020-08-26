package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.QueryValues.SCOPE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.StorageKeys.OAUTH_TOKEN;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc.GetTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc.PostConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.configuration.Xs2aDevelopersProviderConfiguration;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class Xs2aDevelopersAuthenticatorTest {
    private static final String CLIENT_ID = "CLIENT_ID";
    private static final String BASE_URL = "BASE_URL";
    private static final String REDIRECT_URL = "REDIRECT_URL";
    private static final String STATE = "STATE";
    private static final String CONSENT_ID = "CONSENT_ID";
    private static final String AIS_CONSENT_ID = SCOPE + CONSENT_ID;
    private static final String SCA_OAUTH = "SCA_OAUTH";
    private static final String API_CLIENT_AUTHORIZE_URL = "API_CLIENT_AUTHORIZE_URL";
    private static final String CODE = "CODE";
    private static final String ACCESS_TOKEN = "ACCESS_TOKEN";
    private static final String TOKEN_TYPE = "TOKEN_TYPE";
    private static final String REFRESH_TOKEN = "REFRESH_TOKEN";
    private static final long EXPIRES_IN = 1L;
    private static final GetTokenResponse GET_TOKEN_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{\"access_token\" : \""
                            + ACCESS_TOKEN
                            + "\", \"expires_in\" : \""
                            + EXPIRES_IN
                            + "\", \"refresh_token\" : \""
                            + REFRESH_TOKEN
                            + "\", \"token_type\" : \""
                            + TOKEN_TYPE
                            + "\"}",
                    GetTokenResponse.class);
    private static final PostConsentResponse POST_CONSENT_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{\"consentId\" : \""
                            + CONSENT_ID
                            + "\", \"_links\" : {\"scaOAuth\" : \""
                            + SCA_OAUTH
                            + "\"} }",
                    PostConsentResponse.class);

    private Xs2aDevelopersApiClient apiClient;
    private Xs2aDevelopersAuthenticator authenticator;
    private PersistentStorage storage;

    @Before
    public void init() {
        apiClient = mock(Xs2aDevelopersApiClient.class);
        storage = mock(PersistentStorage.class);
        Xs2aDevelopersProviderConfiguration configuration =
                new Xs2aDevelopersProviderConfiguration(CLIENT_ID, BASE_URL, REDIRECT_URL);
        authenticator = new Xs2aDevelopersAuthenticator(apiClient, storage, configuration);
    }

    @Test
    public void shouldRefreshAccessTokenAndConvertItToTinkModel() throws SessionException {
        // given
        when(apiClient.getToken(any())).thenReturn(GET_TOKEN_RESPONSE);

        // when
        OAuth2Token oAuth2Token = authenticator.refreshAccessToken(REFRESH_TOKEN);

        // then
        assertThat(oAuth2Token.getAccessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(oAuth2Token.getExpiresInSeconds()).isEqualTo(EXPIRES_IN);
        assertThat(oAuth2Token.getRefreshToken().isPresent()).isTrue();
        assertThat(oAuth2Token.getRefreshToken().get()).isEqualTo(REFRESH_TOKEN);
        assertThat(oAuth2Token.getTokenType()).isEqualTo(TOKEN_TYPE);
    }

    @Test
    public void shouldStoreAccessTokenInTheStorage() {
        // given
        OAuth2Token token = new OAuth2Token();

        // when
        authenticator.useAccessToken(token);

        // then
        verify(storage).put(OAUTH_TOKEN, token);
        verifyNoMoreInteractions(storage);
    }

    @Test
    public void shouldExchangeAuthorizationCodeAndConvertItToTinkModel() {
        // given
        when(apiClient.getToken(any())).thenReturn(GET_TOKEN_RESPONSE);

        // when
        OAuth2Token oAuth2Token = authenticator.exchangeAuthorizationCode(CODE);

        // then
        assertThat(oAuth2Token.getAccessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(oAuth2Token.getExpiresInSeconds()).isEqualTo(1L);
        assertThat(oAuth2Token.getRefreshToken().isPresent()).isTrue();
        assertThat(oAuth2Token.getRefreshToken().get()).isEqualTo(REFRESH_TOKEN);
        assertThat(oAuth2Token.getTokenType()).isEqualTo(TOKEN_TYPE);
    }

    @Test
    public void shouldBuildAuthorizeUrl() {
        // given
        when(apiClient.createConsent(any())).thenReturn(POST_CONSENT_RESPONSE);
        when(apiClient.buildAuthorizeUrl(STATE, AIS_CONSENT_ID, SCA_OAUTH))
                .thenReturn(new URL(API_CLIENT_AUTHORIZE_URL));

        // when
        URL authorizeUrl = authenticator.buildAuthorizeUrl(STATE);

        // then
        assertThat(authorizeUrl).isEqualTo(new URL(API_CLIENT_AUTHORIZE_URL));
        verify(storage).put(Xs2aDevelopersConstants.StorageKeys.CONSENT_ID, CONSENT_ID);
        verifyNoMoreInteractions(storage);
    }
}
