package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticationValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants.CallbackParams;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants.ErrorDescriptions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants.Errors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.entities.ClientMode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.OpenIdErrorResponse;
import se.tink.backend.aggregation.fakelogmasker.FakeLogMasker;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.constants.ThirdPartyAppConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(MockitoJUnitRunner.class)
public class OpenIdAuthenticationControllerTest {

    private static final String DUMMY_TOKEN_TYPE = "Bearer";
    private static final String DUMMY_ACCESS_TOKEN = "dummy_access_token";
    private static final String DUMMY_REFRESH_TOKEN = "dummy_refresh_token";
    private static final String DUMMY_ID_TOKEN = "dummy_id_token";
    private static final String SERVER_ERROR = "{\"error\":\"server_error\"}";
    private static final String ACCESS_DENIED =
            "{\"error\":\"access denied\",\"error_description\":\"access denied and some random suffix\"}";
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

    @Before
    public void setup() {
        openIdAuthenticationController = initOpenIdAuthenticationController();
    }

    @Test
    public void shouldBreakAutoAuthenticationWhenAccessTokenMissing() {
        // given
        when(persistentStorage.get(
                        OpenIdConstants.PersistentStorageKeys.AIS_ACCESS_TOKEN, OAuth2Token.class))
                .thenReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> openIdAuthenticationController.autoAuthenticate());

        // then
        assertThat(thrown).isExactlyInstanceOf(SessionException.class);
    }

    @Test
    public void shouldAutoAuthenticateSuccessfullyWhenAccessTokenIsNotExpired() {
        // given
        OAuth2Token oAuth2Token = createValidOAuth2Token();
        when(persistentStorage.get(
                        OpenIdConstants.PersistentStorageKeys.AIS_ACCESS_TOKEN, OAuth2Token.class))
                .thenReturn(Optional.of(oAuth2Token));
        // when
        openIdAuthenticationController.autoAuthenticate();

        // then
        verify(apiClient).instantiateAisAuthFilter(oAuth2Token);
    }

    @Test
    public void shouldBreakAutoAuthenticateWhenCanNotRefreshAccessToken() {
        // given
        OAuth2Token oAuth2Token = createNotRefreshableOAuth2Token();
        when(persistentStorage.get(
                        OpenIdConstants.PersistentStorageKeys.AIS_ACCESS_TOKEN, OAuth2Token.class))
                .thenReturn(Optional.of(oAuth2Token));

        // when
        Throwable thrown = catchThrowable(() -> openIdAuthenticationController.autoAuthenticate());

        // then
        assertThat(thrown).isExactlyInstanceOf(SessionException.class);
    }

    @Test
    public void shouldRetryRefreshingAccessTokenOnceIfRuntimeExceptionOccurred() {
        // given
        OAuth2Token invalidOAuth2Token = createInvalidOAuth2Token();
        OAuth2Token refreshedOAuth2Token = createValidOAuth2Token();

        when(persistentStorage.get(
                        OpenIdConstants.PersistentStorageKeys.AIS_ACCESS_TOKEN, OAuth2Token.class))
                .thenReturn(Optional.of(invalidOAuth2Token));
        when(apiClient.refreshAccessToken("dummy_refresh_token", ClientMode.ACCOUNTS))
                .thenThrow(new RuntimeException())
                .thenReturn(refreshedOAuth2Token);

        // when
        openIdAuthenticationController.autoAuthenticate();

        // then
        verify(apiClient, times(2)).refreshAccessToken("dummy_refresh_token", ClientMode.ACCOUNTS);
        verify(apiClient).instantiateAisAuthFilter(refreshedOAuth2Token);
    }

    @Test
    public void shouldAutoAuthenticateSuccessfullyByRefreshingAccessToken() {
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
        when(oAuth2Token.getOptionalRefreshToken()).thenReturn(Optional.of("refreshToken"));
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
        when(oAuth2Token.getOptionalRefreshToken()).thenReturn(Optional.of("refreshToken"));
        when(apiClient.refreshAccessToken("refreshToken", ClientMode.ACCOUNTS))
                .thenThrow(httpResponseException);
        when(httpResponseException.getResponse().getStatus()).thenReturn(500);

        // when
        Throwable thrown = catchThrowable(() -> openIdAuthenticationController.autoAuthenticate());

        // then
        assertThat(thrown).isExactlyInstanceOf(BankServiceException.class);
    }

    @Test
    public void shouldThrowSessionExpiredExceptionWhenCallbackCodeIsMissing() {
        // given
        when(strongAuthenticationState.getSupplementalKey()).thenReturn("randomSupplementalKey");
        openIdAuthenticationController = initOpenIdAuthenticationController();

        when(supplementalInformationHelper.waitForSupplementalInformation(
                        anyString(),
                        eq(ThirdPartyAppConstants.WAIT_FOR_MINUTES),
                        eq(TimeUnit.MINUTES)))
                .thenReturn(Optional.of(new HashMap<>()));

        // when
        Throwable thrown =
                catchThrowable(() -> openIdAuthenticationController.collect("randomString"));

        // then
        assertThat(thrown).isInstanceOf(SessionException.class);
    }

    @Test
    public void
            shouldThrowThirdPartyAppErrorWhenServerErrorOccursAndErrorDescriptionIsServerErrorProcessing() {
        // given
        when(strongAuthenticationState.getSupplementalKey()).thenReturn("randomSupplementalKey");
        openIdAuthenticationController = initOpenIdAuthenticationController();
        Map<String, String> callbackData =
                ImmutableMap.of(
                        CallbackParams.ERROR,
                        Errors.SERVER_ERROR,
                        CallbackParams.ERROR_DESCRIPTION,
                        ErrorDescriptions.SERVER_ERROR_PROCESSING);
        when(supplementalInformationHelper.waitForSupplementalInformation(
                        anyString(), anyLong(), any()))
                .thenReturn(Optional.of(callbackData));

        // when
        Throwable throwable =
                catchThrowable(() -> openIdAuthenticationController.collect("randomString"));

        // then
        assertThat(throwable)
                .isInstanceOfSatisfying(
                        ThirdPartyAppException.class,
                        e -> assertThat(e.getError()).isEqualTo(ThirdPartyAppError.CANCELLED));
    }

    @Test
    public void
            shouldThrowBankServiceErrorWhenServerErrorOccursAndErrorDescriptionIsNotServerErrorProcessing() {
        // given
        when(strongAuthenticationState.getSupplementalKey()).thenReturn("randomSupplementalKey");
        openIdAuthenticationController = initOpenIdAuthenticationController();
        Map<String, String> callbackData =
                ImmutableMap.of(
                        CallbackParams.ERROR,
                        Errors.SERVER_ERROR,
                        CallbackParams.ERROR_DESCRIPTION,
                        "Description of some other server error");
        when(supplementalInformationHelper.waitForSupplementalInformation(
                        anyString(), anyLong(), any()))
                .thenReturn(Optional.of(callbackData));

        // when
        Throwable throwable =
                catchThrowable(() -> openIdAuthenticationController.collect("randomString"));

        // then
        assertThat(throwable)
                .isInstanceOfSatisfying(
                        BankServiceException.class,
                        e ->
                                assertThat(e.getError())
                                        .isEqualTo(BankServiceError.BANK_SIDE_FAILURE));
    }

    @Test
    public void
            shouldThrowSessionExpiredExceptionWhenHttpResponseExceptionOccursDuringExchangingAccessCode() {
        // given
        when(strongAuthenticationState.getSupplementalKey()).thenReturn("randomSupplementalKey");
        openIdAuthenticationController = initOpenIdAuthenticationController();
        Map<String, String> callbackData = ImmutableMap.of(CallbackParams.CODE, "dummyCode");

        when(supplementalInformationHelper.waitForSupplementalInformation(
                        anyString(), anyLong(), any()))
                .thenReturn(Optional.of(callbackData));

        HttpResponseException httpResponseException = mock(HttpResponseException.class);
        when(apiClient.exchangeAccessCode(anyString())).thenThrow(httpResponseException);
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponseException.getResponse()).thenReturn(httpResponse);
        when(httpResponse.getBody(String.class)).thenReturn("Sample response body");

        // when
        Throwable throwable =
                catchThrowable(() -> openIdAuthenticationController.collect("randomString"));

        // then
        assertThat(throwable)
                .isInstanceOfSatisfying(
                        SessionException.class,
                        e -> assertThat(e.getError()).isEqualTo(SessionError.SESSION_EXPIRED));
    }

    @Test
    public void
            shouldThrowSessionExpiredExceptionWhenHttpClientExceptionOccursDuringExchangingAccessCode() {
        // given
        when(strongAuthenticationState.getSupplementalKey()).thenReturn("randomSupplementalKey");
        openIdAuthenticationController = initOpenIdAuthenticationController();
        Map<String, String> callbackData = ImmutableMap.of(CallbackParams.CODE, "dummyCode");

        when(supplementalInformationHelper.waitForSupplementalInformation(
                        anyString(), anyLong(), any()))
                .thenReturn(Optional.of(callbackData));

        HttpClientException httpClientException = mock(HttpClientException.class);
        when(apiClient.exchangeAccessCode(anyString())).thenThrow(httpClientException);
        when(httpClientException.getMessage()).thenReturn("Sample http client exception");

        // when
        Throwable throwable =
                catchThrowable(() -> openIdAuthenticationController.collect("randomString"));

        // then
        assertThat(throwable)
                .isInstanceOfSatisfying(
                        SessionException.class,
                        e -> assertThat(e.getError()).isEqualTo(SessionError.SESSION_EXPIRED));
    }

    @Test
    public void
            shouldThrowSessionExceptionErrorWhenAccessDeniedOccursAndErrorDescriptionIsAccessDenied() {
        // given
        HttpResponseException responseException = mockHttpResponseException(403, ACCESS_DENIED);
        when(apiClient.requestClientCredentials(ClientMode.ACCOUNTS)).thenThrow(responseException);
        // when
        Throwable throwable = catchThrowable(() -> openIdAuthenticationController.init());

        // then
        assertThat(throwable).isExactlyInstanceOf(SessionException.class);
    }

    @Test
    public void shouldThrowSessionExceptionErrorWhenServerErrorOccurs() {
        // given
        HttpResponseException responseException = mockHttpResponseException(400, SERVER_ERROR);
        when(apiClient.requestClientCredentials(ClientMode.ACCOUNTS)).thenThrow(responseException);
        // when
        Throwable throwable = catchThrowable(() -> openIdAuthenticationController.init());

        // then
        assertThat(throwable).isExactlyInstanceOf(SessionException.class);
    }

    @Test
    public void shouldThrowSessionExceptionErrorWhen500HttpStatusOccurs() {
        // given
        HttpResponse response = mock(HttpResponse.class);
        HttpResponseException responseException = new HttpResponseException(null, response);
        given(response.getStatus()).willReturn(500);
        when(apiClient.requestClientCredentials(ClientMode.ACCOUNTS)).thenThrow(responseException);
        // when
        Throwable throwable = catchThrowable(() -> openIdAuthenticationController.init());

        // then
        assertThat(throwable).isExactlyInstanceOf(BankServiceException.class);
    }

    @Test
    public void shouldThrowSessionExceptionErrorWhenClientTokenRequestFailed() {
        // given
        HttpResponse response = mock(HttpResponse.class);
        HttpResponseException responseException = new HttpResponseException(null, response);
        when(apiClient.requestClientCredentials(ClientMode.ACCOUNTS)).thenThrow(responseException);
        // when
        Throwable throwable = catchThrowable(() -> openIdAuthenticationController.init());

        // then
        assertThat(throwable).isExactlyInstanceOf(SessionException.class);
    }

    @Test
    public void shouldThrowSessionExceptionErrorWhenHttpClientExceptionOccurs() {
        // given
        HttpClientException responseException = new HttpClientException(null);
        when(apiClient.requestClientCredentials(ClientMode.ACCOUNTS)).thenThrow(responseException);
        // when
        Throwable throwable = catchThrowable(() -> openIdAuthenticationController.init());

        // then
        assertThat(throwable).isExactlyInstanceOf(SessionException.class);
    }

    private OpenIdAuthenticationController initOpenIdAuthenticationController() {
        return new OpenIdAuthenticationController(
                persistentStorage,
                supplementalInformationHelper,
                apiClient,
                authenticator,
                credentials,
                strongAuthenticationState,
                "callbackUri",
                randomValueGenerator,
                authenticationValidator,
                new FakeLogMasker());
    }

    private OAuth2Token createValidOAuth2Token() {
        return OAuth2Token.create(
                DUMMY_TOKEN_TYPE,
                DUMMY_ACCESS_TOKEN,
                DUMMY_REFRESH_TOKEN,
                DUMMY_ACCESS_EXPIRES_IN_SECONDS,
                200);
    }

    private OAuth2Token createInvalidOAuth2Token() {
        return OAuth2Token.create(
                DUMMY_TOKEN_TYPE,
                DUMMY_ACCESS_TOKEN,
                DUMMY_REFRESH_TOKEN,
                DUMMY_ID_TOKEN,
                0,
                DUMMY_REFRESH_EXPIRES_IN_SECONDS);
    }

    private OAuth2Token createNotRefreshableOAuth2Token() {
        return OAuth2Token.create(DUMMY_TOKEN_TYPE, DUMMY_ACCESS_TOKEN, null, DUMMY_ID_TOKEN, 0, 0);
    }

    private HttpResponseException mockHttpResponseException(int status, String errorResponse) {
        HttpResponse response = mock(HttpResponse.class);
        HttpResponseException responseException = new HttpResponseException(null, response);
        OpenIdErrorResponse openIdErrorResponse =
                SerializationUtils.deserializeFromString(errorResponse, OpenIdErrorResponse.class);
        given(response.getStatus()).willReturn(status);
        given(response.getBody(OpenIdErrorResponse.class)).willReturn(openIdErrorResponse);
        return responseException;
    }
}
