package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.entities.ClientMode;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RunWith(MockitoJUnitRunner.class)
public class OpenIdAuthenticationControllerTest {

    private static final String DUMMY_TOKEN_TYPE = "Bearer";
    private static final String DUMMY_ACCESS_TOKEN = "dummy_access_token";
    private static final String DUMMY_REFRESH_TOKEN = "dummy_refresh_token";
    private static final String DUMMY_ID_TOKEN = "dummy_id_token";
    private static final Long DUMMY_ACCESS_EXPIRES_IN_SECONDS = 1223L;
    private static final Long DUMMY_REFRESH_EXPIRES_IN_SECONDS = 1223L;

    @Mock private PersistentStorage persistentStorage;
    @Mock private SupplementalInformationHelper supplementalInformationHelper;
    @Mock private OpenIdApiClient apiClient;
    @Mock private OpenIdAuthenticator authenticator;
    @Mock private Credentials credentials;
    @Mock private StrongAuthenticationState strongAuthenticationState;
    @Mock private RandomValueGenerator randomValueGenerator;
    @Mock private OpenIdAuthenticationValidator authenticationValidator;

    private OpenIdAuthenticationController openIdAuthenticationController;
    private OAuth2Token oAuth2Token;

    @Before
    public void setup() {
        openIdAuthenticationController =
                new OpenIdAuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        apiClient,
                        authenticator,
                        credentials,
                        strongAuthenticationState,
                        "callbackUri",
                        randomValueGenerator,
                        authenticationValidator);
    }

    @Test
    public void shouldCompleteAutoAuthenticationWhenAccessTokenIsStillValid() {
        // given
        oAuth2Token = createValidOAuth2Token();
        when(persistentStorage.get(
                        OpenIdConstants.PersistentStorageKeys.AIS_ACCESS_TOKEN, OAuth2Token.class))
                .thenReturn(Optional.of(oAuth2Token));
        // when
        openIdAuthenticationController.autoAuthenticate();

        // then
        verify(apiClient).instantiateAisAuthFilter(oAuth2Token);
    }

    @Test
    public void shouldSuccessfullyRefreshedAccessToken() {
        // given
        OAuth2Token invalidOAuth2Token = createInvalidOAuth2Token();
        OAuth2Token refreshedOAuth2Token = createValidOAuth2Token();

        when(persistentStorage.get(
                        OpenIdConstants.PersistentStorageKeys.AIS_ACCESS_TOKEN, OAuth2Token.class))
                .thenReturn(Optional.of(invalidOAuth2Token));
        when(apiClient.refreshAccessToken("dummy_refresh_token", ClientMode.ACCOUNTS))
                .thenReturn(refreshedOAuth2Token);

        // when
        openIdAuthenticationController.autoAuthenticate();

        // then
        verify(apiClient).refreshAccessToken("dummy_refresh_token", ClientMode.ACCOUNTS);
        verify(apiClient).instantiateAisAuthFilter(refreshedOAuth2Token);
    }

    @Test
    public void shouldThrowSessionExceptionForNot5XXStatusResponse() {
        OAuth2Token oAuth2Token = mock(OAuth2Token.class);
        HttpRequest httpRequest = mock(HttpRequest.class);
        HttpResponse httpResponse = mock(HttpResponse.class);
        HttpResponseException httpResponseException =
                new HttpResponseException(httpRequest, httpResponse);

        when(persistentStorage.get(
                        OpenIdConstants.PersistentStorageKeys.AIS_ACCESS_TOKEN, OAuth2Token.class))
                .thenReturn(Optional.of(oAuth2Token));
        when(oAuth2Token.hasAccessExpired()).thenReturn(true);
        when(oAuth2Token.canRefresh()).thenReturn(true);
        when(oAuth2Token.getRefreshToken()).thenReturn(Optional.of("refreshToken"));
        when(apiClient.refreshAccessToken("refreshToken", ClientMode.ACCOUNTS))
                .thenThrow(httpResponseException);
        when(httpResponseException.getResponse().getStatus()).thenReturn(400);
        when(httpResponseException.getResponse().getBody(String.class)).thenReturn(eq(any()));

        // when
        Throwable thrown = catchThrowable(() -> openIdAuthenticationController.autoAuthenticate());

        // then
        assertThat(thrown).isExactlyInstanceOf(SessionException.class);
    }

    @Test
    public void shouldThrowBankServiceExceptionFor5XXStatusResponse() {
        // given
        OAuth2Token oAuth2Token = mock(OAuth2Token.class);
        HttpRequest httpRequest = mock(HttpRequest.class);
        HttpResponse httpResponse = mock(HttpResponse.class);
        HttpResponseException httpResponseException =
                new HttpResponseException(httpRequest, httpResponse);

        when(persistentStorage.get(
                        OpenIdConstants.PersistentStorageKeys.AIS_ACCESS_TOKEN, OAuth2Token.class))
                .thenReturn(Optional.of(oAuth2Token));
        when(oAuth2Token.hasAccessExpired()).thenReturn(true);
        when(oAuth2Token.canRefresh()).thenReturn(true);
        when(oAuth2Token.getRefreshToken()).thenReturn(Optional.of("refreshToken"));
        when(apiClient.refreshAccessToken("refreshToken", ClientMode.ACCOUNTS))
                .thenThrow(httpResponseException);
        when(httpResponseException.getResponse().getStatus()).thenReturn(500);

        // when
        Throwable thrown = catchThrowable(() -> openIdAuthenticationController.autoAuthenticate());

        // then
        assertThat(thrown).isExactlyInstanceOf(BankServiceException.class);
    }

    private OAuth2Token createValidOAuth2Token() {
        oAuth2Token =
                OAuth2Token.create(
                        DUMMY_TOKEN_TYPE,
                        DUMMY_ACCESS_TOKEN,
                        DUMMY_REFRESH_TOKEN,
                        DUMMY_ACCESS_EXPIRES_IN_SECONDS,
                        200);

        return oAuth2Token;
    }

    private OAuth2Token createInvalidOAuth2Token() {
        OAuth2Token invalidOAuth2Token =
                OAuth2Token.create(
                        DUMMY_TOKEN_TYPE,
                        DUMMY_ACCESS_TOKEN,
                        DUMMY_REFRESH_TOKEN,
                        DUMMY_ID_TOKEN,
                        0,
                        DUMMY_REFRESH_EXPIRES_IN_SECONDS);

        return invalidOAuth2Token;
    }
}
