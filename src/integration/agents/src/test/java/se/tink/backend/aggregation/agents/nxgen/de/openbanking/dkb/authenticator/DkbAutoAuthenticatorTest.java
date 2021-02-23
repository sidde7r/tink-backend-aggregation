package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator.DkbAuthenticatorTest.createCredentials;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator.DkbAuthenticatorTest.createDkbAuthenticator;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator.DkbAuthenticatorTest.createDkbStorage;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator.DkbAuthenticatorTest.mockHttpClient;

import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbStorage;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.date.ThreadSafeDateFormat;

@RunWith(JUnitParamsRunner.class)
public class DkbAutoAuthenticatorTest {

    private static final String TEST_CONSENT_ID = "1234consentId";
    private static final String TEST_USERNAME = "1234username";
    private static final String TEST_PASSWORD = "1234password";

    private static final LocalDate DATE_2030_01_01 = LocalDate.parse("2030-01-01");

    private static final OAuth2Token VALID_TOKEN = OAuth2Token.create("x", "x", "x", 1000);
    private static final OAuth2Token EXPIRED_TOKEN = OAuth2Token.create("x", "x", "x", 0);
    private static final String USER_ACCESS_TOKEN_EXPIRED_RESPONSE =
            "{\"tppMessages\":[{\"category\":\"ERROR\",\"code\":\"TOKEN_EXPIRED\",\"path\":\"/v1/consents/asdf\",\"text\":\"Access token expired: xxx\"}],\"_links\":null}";

    private DkbAuthApiClient mockAuthApiClient;
    private DkbSupplementalDataProvider mockSupplementalDataProvider;
    private DkbStorage mockStorage;
    private Credentials mockCredentials;

    private DkbAuthenticator authenticator;

    private Wso2Token testWso2Token;

    @Before
    public void setup() {
        mockAuthApiClient = mock(DkbAuthApiClient.class);
        mockSupplementalDataProvider = mock(DkbSupplementalDataProvider.class);
        mockStorage = mock(DkbStorage.class);
        mockCredentials = mock(Credentials.class);

        authenticator =
                new DkbAuthenticator(
                        mockAuthApiClient,
                        mockSupplementalDataProvider,
                        mockStorage,
                        mockCredentials);

        given(mockCredentials.getField(Field.Key.USERNAME)).willReturn(TEST_USERNAME);
        given(mockCredentials.getField(Field.Key.PASSWORD)).willReturn(TEST_PASSWORD);

        testWso2Token = new Wso2Token();
        testWso2Token.setAccessToken("wso_access");
        testWso2Token.setScope("wso_scope");
        testWso2Token.setTokenType("wso_type");
        testWso2Token.setExpiresIn(124L);
    }

    @Test
    public void shouldThrowSessionExpiredIfNoStoredConsent() {
        // given
        given(mockStorage.getConsentId()).willReturn(Optional.empty());

        // when
        Throwable throwable = catchThrowable(() -> authenticator.autoAuthenticate());

        // then
        assertThat(throwable)
                .isInstanceOf(SessionException.class)
                .hasMessage("Cause: SessionError.SESSION_EXPIRED");
        verify(mockStorage).getConsentId();
        verifyNoMoreInteractionsOnAnyMock();
    }

    @Test
    public void shouldFinishWithoutExceptionIfStoredConsentValid() {
        // given
        given(mockStorage.getConsentId()).willReturn(Optional.of(TEST_CONSENT_ID));
        given(mockStorage.getWso2OAuthToken()).willReturn(VALID_TOKEN);

        given(mockAuthApiClient.getConsentDetails(TEST_CONSENT_ID))
                .willReturn(consentWithStatus("valid"));

        // when
        Throwable throwable = catchThrowable(() -> authenticator.autoAuthenticate());

        // then
        assertThat(throwable).isNull();
        verify(mockStorage, times(2)).getConsentId();
        verify(mockStorage).getWso2OAuthToken();
        verify(mockAuthApiClient).getConsentDetails(TEST_CONSENT_ID);
        verify(mockCredentials).setSessionExpiryDate(DATE_2030_01_01);
        verifyNoMoreInteractionsOnAnyMock();
    }

    @Test
    @Parameters({
        "expired, Cause: SessionError.CONSENT_EXPIRED",
        "revokedByPsu, Cause: SessionError.CONSENT_REVOKED_BY_USER",
        "AnythingElse, Cause: SessionError.SESSION_EXPIRED",
    })
    public void shouldThrowProperSessionExceptionDependingOnConsentStatus(
            String consentStatus, String expectedCause) {
        // given
        given(mockStorage.getConsentId()).willReturn(Optional.of(TEST_CONSENT_ID));
        given(mockStorage.getWso2OAuthToken()).willReturn(VALID_TOKEN);

        given(mockAuthApiClient.getConsentDetails(TEST_CONSENT_ID))
                .willReturn(consentWithStatus(consentStatus));

        // when
        Throwable throwable = catchThrowable(() -> authenticator.autoAuthenticate());

        // then
        assertThat(throwable).isInstanceOf(SessionException.class).hasMessage(expectedCause);
        verify(mockStorage, times(2)).getConsentId();
        verify(mockStorage).getWso2OAuthToken();
        verify(mockAuthApiClient).getConsentDetails(TEST_CONSENT_ID);
        verifyNoMoreInteractionsOnAnyMock();
    }

    @Test
    public void shouldGetNewWsoTokenAndStoreItIfExpired() {
        // given
        given(mockStorage.getConsentId()).willReturn(Optional.of(TEST_CONSENT_ID));
        given(mockStorage.getWso2OAuthToken()).willReturn(EXPIRED_TOKEN);
        given(mockAuthApiClient.getWso2Token()).willReturn(testWso2Token);
        given(mockAuthApiClient.getConsentDetails(TEST_CONSENT_ID))
                .willReturn(consentWithStatus("valid"));

        // when
        Throwable throwable = catchThrowable(() -> authenticator.autoAuthenticate());

        // then
        assertThat(throwable).isNull();
        verify(mockStorage, times(2)).getConsentId();
        verify(mockAuthApiClient).getWso2Token();
        verify(mockStorage).getWso2OAuthToken();
        verify(mockStorage).setWso2OAuthToken(eq(testWso2Token.toOAuth2Token()));
        verify(mockAuthApiClient).getConsentDetails(TEST_CONSENT_ID);
        verify(mockCredentials).setSessionExpiryDate(DATE_2030_01_01);
        verifyNoMoreInteractionsOnAnyMock();
    }

    @Test
    public void shouldCompleteAfterSuccessfullyRefreshingUserAccessToken() {
        // given
        given(mockStorage.getConsentId()).willReturn(Optional.of(TEST_CONSENT_ID));
        given(mockStorage.getWso2OAuthToken()).willReturn(VALID_TOKEN);
        HttpResponse testHttpResponse = getTestHttpResponse();
        given(mockAuthApiClient.getConsentDetails(TEST_CONSENT_ID))
                .willThrow(new HttpResponseException(null, testHttpResponse))
                .willReturn(consentWithStatus("valid"));

        AuthResult testAuthResult = getTestAuthResult(true);
        given(mockAuthApiClient.authenticate1stFactor(TEST_USERNAME, TEST_PASSWORD))
                .willReturn(testAuthResult);

        // when
        Throwable throwable = catchThrowable(() -> authenticator.autoAuthenticate());

        // then
        assertThat(throwable).isNull();
        verify(mockStorage, times(3)).getConsentId();
        verify(mockStorage).getWso2OAuthToken();
        verify(mockStorage).setAccessToken(testAuthResult.toOAuth2Token());
        verify(mockAuthApiClient, times(2)).getConsentDetails(TEST_CONSENT_ID);
        verify(mockAuthApiClient).authenticate1stFactor(TEST_USERNAME, TEST_PASSWORD);
        verify(mockCredentials).getField(Field.Key.USERNAME);
        verify(mockCredentials).getField(Field.Key.PASSWORD);
        verify(mockCredentials).setSessionExpiryDate(DATE_2030_01_01);
        verifyNoMoreInteractionsOnAnyMock();
    }

    @Test
    public void shouldThrowIncorrectCredentialsAfterReceiving400For1stFactor() {
        // given
        given(mockStorage.getConsentId()).willReturn(Optional.of(TEST_CONSENT_ID));
        given(mockStorage.getWso2OAuthToken()).willReturn(VALID_TOKEN);
        HttpResponse testHttpResponse = getTestHttpResponse();
        given(mockAuthApiClient.getConsentDetails(TEST_CONSENT_ID))
                .willThrow(new HttpResponseException(null, testHttpResponse))
                .willReturn(consentWithStatus("valid"));

        HttpResponse response = mock(HttpResponse.class);
        HttpResponseException httpResponseException = new HttpResponseException(null, response);
        when(response.getStatus()).thenReturn(400);
        when(response.hasBody()).thenReturn(false);
        given(mockAuthApiClient.authenticate1stFactor(TEST_USERNAME, TEST_PASSWORD))
                .willThrow(httpResponseException);

        // when
        Throwable throwable = catchThrowable(() -> authenticator.autoAuthenticate());

        // then
        assertThat(throwable)
                .isExactlyInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.INCORRECT_CREDENTIALS");
    }

    @Test
    public void shouldThrowSessionExpiredWhenFailedToRefreshUserAccessToken() {
        // given
        given(mockStorage.getConsentId()).willReturn(Optional.of(TEST_CONSENT_ID));
        given(mockStorage.getWso2OAuthToken()).willReturn(VALID_TOKEN);

        HttpResponse testHttpResponse = getTestHttpResponse();
        given(mockAuthApiClient.getConsentDetails(TEST_CONSENT_ID))
                .willThrow(new HttpResponseException(null, testHttpResponse));

        AuthResult testAuthResult = getTestAuthResult(false);
        given(mockAuthApiClient.authenticate1stFactor(TEST_USERNAME, TEST_PASSWORD))
                .willReturn(testAuthResult);

        // when
        Throwable throwable = catchThrowable(() -> authenticator.autoAuthenticate());

        // then
        assertThat(throwable)
                .isInstanceOf(SessionException.class)
                .hasMessage("Failed to gather new oauth token during auto authentication.");
        verify(mockStorage, times(2)).getConsentId();
        verify(mockStorage).getWso2OAuthToken();
        verify(mockAuthApiClient, times(1)).getConsentDetails(TEST_CONSENT_ID);
        verify(mockAuthApiClient).authenticate1stFactor(TEST_USERNAME, TEST_PASSWORD);
        verify(mockCredentials).getField(Field.Key.USERNAME);
        verify(mockCredentials).getField(Field.Key.PASSWORD);
        verifyNoMoreInteractionsOnAnyMock();
    }

    @Test
    @SneakyThrows
    public void whenUserIsAutoAuthenticatedThenSessionExpiryDateIsSet() {
        // given
        Date currentSessionExpiryDate =
                ThreadSafeDateFormat.FORMATTER_DAILY_DEFAULT_TIMEZONE.parse("2029-01-01");
        Date newSessionExpiryDate =
                ThreadSafeDateFormat.FORMATTER_DAILY_DEFAULT_TIMEZONE.parse("2030-01-01");
        TinkHttpClient tinkHttpClient = mockHttpClient();
        Credentials credentials = createCredentials(currentSessionExpiryDate);
        DkbStorage dkbStorage = createDkbStorage();
        DkbAuthenticator dkbAuthenticator =
                createDkbAuthenticator(tinkHttpClient, credentials, dkbStorage);

        // when
        dkbAuthenticator.autoAuthenticate();

        // then
        assertThat(credentials.getSessionExpiryDate()).isEqualToIgnoringHours(newSessionExpiryDate);
    }

    private HttpResponse getTestHttpResponse() {
        HttpResponse httpResponse = mock(HttpResponse.class);
        given(httpResponse.getStatus()).willReturn(401);
        given(httpResponse.hasBody()).willReturn(true);
        given(httpResponse.getBody(String.class)).willReturn(USER_ACCESS_TOKEN_EXPIRED_RESPONSE);
        return httpResponse;
    }

    private AuthResult getTestAuthResult(boolean success) {
        AuthResult authResult = new AuthResult();
        authResult.setAccessToken("T");
        authResult.setReturnCode(success ? "CORRECT" : "ANYTHING_ELSE");
        return authResult;
    }

    private ConsentDetailsResponse consentWithStatus(String status) {
        ConsentDetailsResponse consent = new ConsentDetailsResponse();
        consent.setConsentStatus(status);
        consent.setValidUntil(DATE_2030_01_01);
        return consent;
    }

    private void verifyNoMoreInteractionsOnAnyMock() {
        verifyNoMoreInteractions(
                mockAuthApiClient, mockSupplementalDataProvider, mockStorage, mockCredentials);
    }
}
