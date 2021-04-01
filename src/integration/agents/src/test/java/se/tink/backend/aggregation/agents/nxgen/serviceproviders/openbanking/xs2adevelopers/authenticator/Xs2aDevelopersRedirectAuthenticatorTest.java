package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.QueryValues.SCOPE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.StorageKeys.LINKS;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.StorageKeys.OAUTH_TOKEN;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.MediaType;
import junitparams.JUnitParamsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.entities.ConsentLinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc.WellKnownResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.configuration.Xs2aDevelopersProviderConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.MockRandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.CallbackParams;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(JUnitParamsRunner.class)
public class Xs2aDevelopersRedirectAuthenticatorTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/xs2adevelopers/resources/";
    private static final String SUPPLEMENTAL_KEY = "supplementalKey";
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
    private static final ConsentLinksEntity CONSENT_LINKS_ENTITY =
            SerializationUtils.deserializeFromString(
                    " {\"scaOAuth\" : \"" + SCA_OAUTH + "\"}", ConsentLinksEntity.class);
    private static final TokenResponse GET_TOKEN_RESPONSE =
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
                    TokenResponse.class);
    private static final HttpResponse POST_CONSENT_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{\"consentId\" : \""
                            + CONSENT_ID
                            + "\", \"_links\" : {\"scaOAuth\" : \""
                            + SCA_OAUTH
                            + "\"} }",
                    HttpResponse.class);
    private static final Xs2aDevelopersProviderConfiguration xs2aDevelopersProviderConfiguration =
            new Xs2aDevelopersProviderConfiguration(CLIENT_ID, BASE_URL, REDIRECT_URL);

    private Xs2aDevelopersApiClient apiClient;
    private LocalDateTimeSource localDateTimeSource;
    private Xs2aDevelopersAuthenticatorHelper authenticator;
    private PersistentStorage storage;

    @Before
    public void init() {
        apiClient = mock(Xs2aDevelopersApiClient.class);
        storage = mock(PersistentStorage.class);
        localDateTimeSource = mock(LocalDateTimeSource.class);
        when(localDateTimeSource.now()).thenReturn(LocalDateTime.of(1234, 5, 12, 12, 30, 40));
        when(storage.get(LINKS, ConsentLinksEntity.class))
                .thenReturn(Optional.of(CONSENT_LINKS_ENTITY));
        when(storage.get(StorageKeys.CONSENT_ID, String.class)).thenReturn(Optional.of(CONSENT_ID));
        authenticator =
                new Xs2aDevelopersAuthenticatorHelper(
                        apiClient,
                        storage,
                        xs2aDevelopersProviderConfiguration,
                        localDateTimeSource,
                        new Credentials());
    }

    @Test
    public void should_refresh_access_token_and_convert_it_to_tink_model() throws SessionException {
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
    public void should_store_access_token_in_the_storage() {
        // given
        OAuth2Token token = new OAuth2Token();

        // when
        authenticator.useAccessToken(token);

        // then
        verify(storage).put(OAUTH_TOKEN, token);
        verifyNoMoreInteractions(storage);
    }

    @Test
    public void should_exchange_authorization_code_and_convert_it_to_tink_model() {
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
    public void should_build_authorize_url() {
        // given
        when(apiClient.createConsent(any(), anyString())).thenReturn(POST_CONSENT_RESPONSE);
        when(apiClient.buildAuthorizeUrl(STATE, AIS_CONSENT_ID, SCA_OAUTH))
                .thenReturn(new URL(API_CLIENT_AUTHORIZE_URL));

        // when
        URL authorizeUrl = authenticator.buildAuthorizeUrl(STATE);

        // then
        assertThat(authorizeUrl).isEqualTo(new URL(API_CLIENT_AUTHORIZE_URL));
    }

    @Test
    public void when_consent_is_not_valid_auto_authenticate_throws_session_exception() {
        TinkHttpClient tinkHttpClient = mockHttpClient();
        PersistentStorage persistentStorage = new PersistentStorage();
        persistentStorage.put(StorageKeys.CONSENT_ID, CONSENT_ID);
        Credentials credentials = createCredentials(new Date());

        Xs2aDevelopersAuthenticatorHelper xs2ADevelopersAuthenticatorHelper =
                createXs2aDevelopersAuthenticator(tinkHttpClient, persistentStorage, credentials);
        Xs2aDevelopersRedirectAuthenticator authenticationController =
                createXs2aDevelopersRedirectAuthenticatior(
                        xs2ADevelopersAuthenticatorHelper, credentials, persistentStorage);

        // when
        Throwable thrown = catchThrowable(() -> authenticationController.autoAuthenticate());

        // then
        assertThat(thrown).isInstanceOf(SessionException.class);
    }

    @Test
    public void buildAuthorizeUrl_should_request_for_wellKnownUrl_if_needed() {
        // given
        final URL expectedAuthorizeUrl =
                new URL("https://psd2.bank.domain/authorize/dsadfdsfedwsf?someParam1=value1");
        final String postConsentResponseWithWellKnownUri =
                "{\"scaOAuth\" : \"" + "http://test.domain/.well-known/121234324234" + "\"} }";

        final WellKnownResponse postWellKnownUriResponse =
                SerializationUtils.deserializeFromString(
                        "{\"authorization_endpoint\" : \"https://psd2.bank.domain/authorize/dsadfdsfedwsf\"}",
                        WellKnownResponse.class);
        when(apiClient.getAuthorizationEndpointFromWellKnownURI(anyString()))
                .thenReturn(postWellKnownUriResponse.getAuthorizationEndpoint());
        when(apiClient.buildAuthorizeUrl(
                        any(), any(), eq(postWellKnownUriResponse.getAuthorizationEndpoint())))
                .thenReturn(expectedAuthorizeUrl);
        PersistentStorage persistentStorage = new PersistentStorage();
        persistentStorage.put(StorageKeys.LINKS, postConsentResponseWithWellKnownUri);
        persistentStorage.put(StorageKeys.CONSENT_ID, CONSENT_ID);
        Credentials credentials = new Credentials();
        credentials.setType(CredentialsTypes.THIRD_PARTY_APP);
        Xs2aDevelopersAuthenticatorHelper authenticatiorWithRequestForWellKnownUrlNeeded =
                new Xs2aDevelopersAuthenticatorHelper(
                        apiClient,
                        persistentStorage,
                        xs2aDevelopersProviderConfiguration,
                        localDateTimeSource,
                        credentials);

        // when
        URL result = authenticatiorWithRequestForWellKnownUrlNeeded.buildAuthorizeUrl("dummyState");

        // then
        assertThat(result).isEqualTo(expectedAuthorizeUrl);
    }

    private Credentials createCredentials(Date sessionExpiryDate) {
        Credentials credentials = new Credentials();
        credentials.setType(CredentialsTypes.PASSWORD);
        credentials.setSessionExpiryDate(sessionExpiryDate);
        return credentials;
    }

    public Xs2aDevelopersRedirectAuthenticator createXs2aDevelopersRedirectAuthenticatior(
            Xs2aDevelopersAuthenticatorHelper xs2ADevelopersAuthenticatorHelper,
            Credentials credentials,
            PersistentStorage persistentStorage) {
        SupplementalInformationHelper supplementalInformationHelper =
                createSupplementalInformationHelper();

        StrongAuthenticationState strongAuthenticationState = createStrongAuthenticationState();

        final OAuth2AuthenticationController oAuth2Controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        xs2ADevelopersAuthenticatorHelper,
                        credentials,
                        strongAuthenticationState);

        return new Xs2aDevelopersRedirectAuthenticator(
                oAuth2Controller, supplementalInformationHelper, xs2ADevelopersAuthenticatorHelper);
    }

    private StrongAuthenticationState createStrongAuthenticationState() {
        StrongAuthenticationState strongAuthenticationState = mock(StrongAuthenticationState.class);
        when(strongAuthenticationState.getSupplementalKey()).thenReturn(SUPPLEMENTAL_KEY);
        return strongAuthenticationState;
    }

    private SupplementalInformationHelper createSupplementalInformationHelper() {
        SupplementalInformationHelper supplementalInformationHelper =
                mock(SupplementalInformationHelper.class);
        Map<String, String> callbackData = Collections.singletonMap(CallbackParams.CODE, CODE);
        when(supplementalInformationHelper.waitForSupplementalInformation(
                        eq(SUPPLEMENTAL_KEY), anyLong(), any(TimeUnit.class)))
                .thenReturn(Optional.of(callbackData));
        return supplementalInformationHelper;
    }

    private Xs2aDevelopersAuthenticatorHelper createXs2aDevelopersAuthenticator(
            TinkHttpClient tinkHttpClient,
            PersistentStorage persistentStorage,
            Credentials credentials) {

        Xs2aDevelopersApiClient xs2aDevelopersApiClient =
                new Xs2aDevelopersApiClient(
                        tinkHttpClient,
                        persistentStorage,
                        xs2aDevelopersProviderConfiguration,
                        true,
                        "userIp",
                        new MockRandomValueGenerator());
        return new Xs2aDevelopersAuthenticatorHelper(
                xs2aDevelopersApiClient,
                persistentStorage,
                xs2aDevelopersProviderConfiguration,
                localDateTimeSource,
                credentials);
    }

    public TinkHttpClient mockHttpClient() {
        TinkHttpClient tinkHttpClient = mock(TinkHttpClient.class);
        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        when(tinkHttpClient.request(any(URL.class))).thenReturn(requestBuilder);
        when(tinkHttpClient.request(any(String.class))).thenReturn(requestBuilder);
        when(requestBuilder.accept(any(MediaType.class))).thenReturn(requestBuilder);
        when(requestBuilder.type(any(String.class))).thenReturn(requestBuilder);
        when(requestBuilder.header(any(), any())).thenReturn(requestBuilder);
        when(requestBuilder.body(any(Object.class))).thenReturn(requestBuilder);
        when(requestBuilder.body(any(Object.class), anyString())).thenReturn(requestBuilder);
        when(requestBuilder.get(ConsentStatusResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "invalid_consent_status_response.json")
                                        .toFile(),
                                ConsentStatusResponse.class));
        return tinkHttpClient;
    }
}
