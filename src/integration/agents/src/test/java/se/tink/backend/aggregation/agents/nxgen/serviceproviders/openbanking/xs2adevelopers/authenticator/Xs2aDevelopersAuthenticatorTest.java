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
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.StorageKeys.OAUTH_TOKEN;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
import se.tink.backend.aggregation.agents.contexts.SystemUpdater;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.framework.context.AgentTestContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc.WellKnownResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.configuration.Xs2aDevelopersProviderConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ActualLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.MockRandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.CallbackParams;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(JUnitParamsRunner.class)
public class Xs2aDevelopersAuthenticatorTest {

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
    private static final ConsentResponse POST_CONSENT_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{\"consentId\" : \""
                            + CONSENT_ID
                            + "\", \"_links\" : {\"scaOAuth\" : \""
                            + SCA_OAUTH
                            + "\"} }",
                    ConsentResponse.class);

    private Xs2aDevelopersApiClient apiClient;
    private LocalDateTimeSource localDateTimeSource;
    private Xs2aDevelopersAuthenticator authenticator;
    private PersistentStorage storage;

    @Before
    public void init() {
        apiClient = mock(Xs2aDevelopersApiClient.class);
        storage = mock(PersistentStorage.class);
        Xs2aDevelopersProviderConfiguration configuration =
                new Xs2aDevelopersProviderConfiguration(CLIENT_ID, BASE_URL, REDIRECT_URL);
        localDateTimeSource = mock(LocalDateTimeSource.class);

        authenticator =
                new Xs2aDevelopersAuthenticator(
                        apiClient,
                        storage,
                        configuration,
                        localDateTimeSource,
                        new Credentials(),
                        false);
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
        when(apiClient.createConsent(any())).thenReturn(POST_CONSENT_RESPONSE);
        when(apiClient.buildAuthorizeUrl(STATE, AIS_CONSENT_ID, SCA_OAUTH))
                .thenReturn(new URL(API_CLIENT_AUTHORIZE_URL));
        when(localDateTimeSource.now()).thenReturn(LocalDateTime.of(1234, 5, 12, 12, 30, 40));

        // when
        URL authorizeUrl = authenticator.buildAuthorizeUrl(STATE);

        // then
        assertThat(authorizeUrl).isEqualTo(new URL(API_CLIENT_AUTHORIZE_URL));
        verify(storage).put(Xs2aDevelopersConstants.StorageKeys.CONSENT_ID, CONSENT_ID);
        verifyNoMoreInteractions(storage);
    }

    @Test
    public void when_user_is_authenticated_then_session_expiry_date_is_set() {
        // given
        Date date = toDate("2030-01-01");

        TinkHttpClient tinkHttpClient = mockHttpClient();
        PersistentStorage persistentStorage = new PersistentStorage();
        Credentials credentials = new Credentials();
        credentials.setType(CredentialsTypes.THIRD_PARTY_APP);

        Xs2aDevelopersAuthenticator xs2aDevelopersAuthenticator =
                createXs2aDevelopersAuthenticator(
                        tinkHttpClient, persistentStorage, credentials, false);

        AutoAuthenticationController autoAuthenticationController =
                createAutoAuthenticationController(
                        xs2aDevelopersAuthenticator, credentials, persistentStorage, true);

        // when
        autoAuthenticationController.authenticate(credentials);

        // then
        assertThat(credentials.getSessionExpiryDate()).isEqualTo(date);
    }

    @Test
    public void when_consent_is_not_valid_auto_authenticate_throws_session_exception() {
        TinkHttpClient tinkHttpClient = mockHttpClient();
        PersistentStorage persistentStorage = new PersistentStorage();
        Credentials credentials = createCredentials(new Date());

        Xs2aDevelopersAuthenticator xs2aDevelopersAuthenticator =
                createXs2aDevelopersAuthenticator(
                        tinkHttpClient, persistentStorage, credentials, false);
        AutoAuthenticationController autoAuthenticationController =
                createAutoAuthenticationController(
                        xs2aDevelopersAuthenticator, credentials, persistentStorage, false);

        // when
        Throwable thrown =
                catchThrowable(() -> autoAuthenticationController.authenticate(credentials));

        // then
        assertThat(thrown).isInstanceOf(SessionException.class);
    }

    @Test
    public void when_auto_authenticate_then_session_expiry_date_should_be_set() {
        // given
        Date currentSessionExpiryDate = toDate("2029-01-01");
        Date newSessionExpiryDate = toDate("2030-01-01");

        TinkHttpClient tinkHttpClient = mockHttpClient();
        PersistentStorage persistentStorage = createPersistentStorage();
        Credentials credentials = createCredentials(currentSessionExpiryDate);

        Xs2aDevelopersAuthenticator xs2aDevelopersAuthenticator =
                createXs2aDevelopersAuthenticator(
                        tinkHttpClient, persistentStorage, credentials, false);
        AutoAuthenticationController autoAuthenticationController =
                createAutoAuthenticationController(
                        xs2aDevelopersAuthenticator, credentials, persistentStorage, false);

        // when
        autoAuthenticationController.authenticate(credentials);

        // then
        assertThat(credentials.getSessionExpiryDate()).isEqualTo(newSessionExpiryDate);
    }

    @Test
    public void buildAuthorizeUrl_should_request_for_wellKnownUrl_if_needed() {
        // given
        TinkHttpClient tinkHttpClient = mockHttpClient();
        PersistentStorage persistentStorage = new PersistentStorage();
        Credentials credentials = new Credentials();
        credentials.setType(CredentialsTypes.THIRD_PARTY_APP);
        Xs2aDevelopersAuthenticator authenticatiorWithRequestForWellKnownUrlNeeded =
                createXs2aDevelopersAuthenticator(
                        tinkHttpClient, persistentStorage, credentials, true);

        // when
        authenticatiorWithRequestForWellKnownUrlNeeded.buildAuthorizeUrl("dummyState");

        // then
        verify(tinkHttpClient)
                .request(
                        "https://psd.xs2a-api.com/public/berlingroup/authorize/55d7b2c8-d120-441c-ab3c-ca930e2f6ec9");
    }

    private Credentials createCredentials(Date sessionExpiryDate) {
        Credentials credentials = new Credentials();
        credentials.setType(CredentialsTypes.PASSWORD);
        credentials.setSessionExpiryDate(sessionExpiryDate);
        return credentials;
    }

    private PersistentStorage createPersistentStorage() {
        PersistentStorage persistentStorage = new PersistentStorage();
        OAuth2Token oAuth2Token = OAuth2Token.create("type", "access", "refresh", 0, 0);
        persistentStorage.put(PersistentStorageKeys.OAUTH_2_TOKEN, oAuth2Token);
        persistentStorage.put(StorageKeys.CONSENT_ID, "1604575204-ba78d90");
        return persistentStorage;
    }

    public AutoAuthenticationController createAutoAuthenticationController(
            Xs2aDevelopersAuthenticator xs2aDevelopersAuthenticator,
            Credentials credentials,
            PersistentStorage persistentStorage,
            Boolean manual) {

        RefreshInformationRequest refreshInformationRequest =
                RefreshInformationRequest.builder().credentials(credentials).manual(manual).build();
        SystemUpdater systemUpdater = new AgentTestContext(credentials);

        SupplementalInformationHelper supplementalInformationHelper =
                createSupplementalInformationHelper();

        StrongAuthenticationState strongAuthenticationState = createStrongAuthenticationState();

        final OAuth2AuthenticationController oAuth2Controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        xs2aDevelopersAuthenticator,
                        credentials,
                        strongAuthenticationState);

        Xs2aDevelopersOAuth2AuthenticatorController xs2aController =
                new Xs2aDevelopersOAuth2AuthenticatorController(
                        oAuth2Controller, xs2aDevelopersAuthenticator);

        return new AutoAuthenticationController(
                refreshInformationRequest,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        xs2aController, supplementalInformationHelper),
                xs2aController);
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

    private Xs2aDevelopersAuthenticator createXs2aDevelopersAuthenticator(
            TinkHttpClient tinkHttpClient,
            PersistentStorage persistentStorage,
            Credentials credentials,
            boolean isRequestForWellKnownUrlNeeded) {
        Xs2aDevelopersProviderConfiguration xs2aDevelopersProviderConfiguration =
                new Xs2aDevelopersProviderConfiguration("clientId", "baseUrl", "redirectUrl");

        Xs2aDevelopersApiClient xs2aDevelopersApiClient =
                new Xs2aDevelopersApiClient(
                        tinkHttpClient,
                        persistentStorage,
                        xs2aDevelopersProviderConfiguration,
                        true,
                        "userIp",
                        new MockRandomValueGenerator());

        LocalDateTimeSource localDateTimeSource = new ActualLocalDateTimeSource();
        return new Xs2aDevelopersAuthenticator(
                xs2aDevelopersApiClient,
                persistentStorage,
                xs2aDevelopersProviderConfiguration,
                localDateTimeSource,
                credentials,
                isRequestForWellKnownUrlNeeded);
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
        when(requestBuilder.post(ConsentResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "thirdparty_callback_response.json")
                                        .toFile(),
                                ConsentResponse.class));
        when(requestBuilder.post(TokenResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "token_response.json").toFile(),
                                TokenResponse.class));
        when(requestBuilder.get(ConsentDetailsResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "consent_details_response.json").toFile(),
                                ConsentDetailsResponse.class));
        when(requestBuilder.get(ConsentStatusResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "consent_status_response.json").toFile(),
                                ConsentStatusResponse.class));
        when(requestBuilder.get(WellKnownResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "well_known_response.json").toFile(),
                                WellKnownResponse.class));
        return tinkHttpClient;
    }

    private Date toDate(String date) {
        return Date.from(
                LocalDate.parse(date).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }
}
