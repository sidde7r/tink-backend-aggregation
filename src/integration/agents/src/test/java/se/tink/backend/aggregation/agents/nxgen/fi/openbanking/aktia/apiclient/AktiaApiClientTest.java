package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaTestFixtures.ACCOUNT_ID;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaTestFixtures.API_SERVER_URL;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaTestFixtures.AUTH_SERVER_URL;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaTestFixtures.BASIC_AUTH_HEADER_VALUE;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaTestFixtures.CONTINUATION_KEY;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaTestFixtures.CORRECT_PASSWORD;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaTestFixtures.OTP_CODE;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaTestFixtures.USERNAME;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaTestFixtures.createOAuth2Token;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaTestFixtures.createSuccessfulAccountsSummaryResponse;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaTestFixtures.createSuccessfulLoginDetailsResponseWithOtpRequired;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaTestFixtures.createSuccessfulOtpAuthenticationResponse;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaTestFixtures.createTokenResponseDto;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaTestFixtures.createTransactionsAndLockedEventsResponseWithContinuationKey;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaTestFixtures.createTransactionsAndLockedEventsResponseWithoutContinuationKey;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaTestFixtures.getTokenRequest;

import java.util.Optional;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.request.OtpAuthenticationRequestDto;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.request.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response.OtpAuthenticationResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response.TokenResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.response.AccountsSummaryResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.response.LoginDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.response.OtpAuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.response.TransactionsAndLockedEventsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.configuration.AktiaConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2TokenStorage;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class AktiaApiClientTest {

    private static final String AUTH_TOKEN_URL =
            AUTH_SERVER_URL + "/cauth/oauth2/mobileauth/access_token";

    private AktiaApiClient aktiaApiClient;

    private TinkHttpClient httpClientMock;

    private OAuth2TokenStorage tokenStorageMock;

    @Before
    public void setUp() {
        final AktiaConfiguration aktiaConfigurationMock = createAktiaConfigurationMock();

        httpClientMock = mock(TinkHttpClient.class);
        tokenStorageMock = mock(OAuth2TokenStorage.class);

        final OAuth2Token oAuth2Token = createOAuth2Token();
        when(tokenStorageMock.getToken()).thenReturn(Optional.of(oAuth2Token));

        aktiaApiClient =
                new AktiaApiClient(httpClientMock, aktiaConfigurationMock, tokenStorageMock);
    }

    @Test
    public void shouldRetrieveAccessToken() {
        // given
        final TokenResponseDto expectedTokenResponse = createTokenResponseDto();
        final RequestBuilder requestBuilderMock = setUpHttpClientMockForAuth(expectedTokenResponse);
        final ArgumentCaptor<TokenRequest> tokenRequestCaptor =
                ArgumentCaptor.forClass(TokenRequest.class);
        when(requestBuilderMock.body(tokenRequestCaptor.capture(), anyString()))
                .thenReturn(requestBuilderMock);

        // when
        final TokenResponseDto returnedResponse =
                aktiaApiClient.retrieveAccessToken(USERNAME, CORRECT_PASSWORD);

        // then
        assertThat(returnedResponse).isEqualTo(expectedTokenResponse);

        final String expectedTokenRequest = getTokenRequest();
        final String actualTokenRequest = tokenRequestCaptor.getValue().getBodyValue();
        assertThat(actualTokenRequest).isEqualTo(expectedTokenRequest);
    }

    @Test
    public void shouldThrowIncorrectCredentialsException() {
        // given
        setupHttpClientMockForAuthException();
        // then
        Throwable thrown =
                catchThrowable(
                        () -> aktiaApiClient.retrieveAccessToken(USERNAME, CORRECT_PASSWORD));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.INCORRECT_CREDENTIALS");
    }

    @Test
    public void shouldGetLoginDetails() {
        // given
        final String url = API_SERVER_URL + "/api/login/details";
        final LoginDetailsResponse expectedLoginDetailsResponse =
                createSuccessfulLoginDetailsResponseWithOtpRequired();

        setUpHttpClientMockForApi(url, expectedLoginDetailsResponse.getLoginDetailsResponseDto());

        // when
        final LoginDetailsResponse returnedResponse = aktiaApiClient.getLoginDetails();

        // then
        assertThat(returnedResponse).isEqualTo(expectedLoginDetailsResponse);
    }

    @Test
    public void shouldAuthenticateWithOtp() {
        // given
        final OtpAuthenticationResponse expectedOtpAuthenticationResponse =
                createSuccessfulOtpAuthenticationResponse();

        final RequestBuilder requestBuilderMock =
                setUpHttpClientMockForAuthWithOtp(
                        expectedOtpAuthenticationResponse.getOtpAuthenticationResponseDto());
        final ArgumentCaptor<OtpAuthenticationRequestDto> otpAuthenticationRequestCaptor =
                ArgumentCaptor.forClass(OtpAuthenticationRequestDto.class);
        when(requestBuilderMock.body(
                        otpAuthenticationRequestCaptor.capture(), eq(MediaType.APPLICATION_JSON)))
                .thenReturn(requestBuilderMock);

        // when
        final OtpAuthenticationResponse returnedResponse =
                aktiaApiClient.authenticateWithOtp(OTP_CODE);

        // then
        assertThat(returnedResponse).isEqualTo(expectedOtpAuthenticationResponse);

        final OtpAuthenticationRequestDto expectedOtpAuthenticationRequest =
                new OtpAuthenticationRequestDto(OTP_CODE);
        final OtpAuthenticationRequestDto actualOtpAuthenticationRequest =
                otpAuthenticationRequestCaptor.getValue();
        assertThat(actualOtpAuthenticationRequest).isEqualTo(expectedOtpAuthenticationRequest);
    }

    @Test
    public void shouldGetAccountsSummary() {
        // given
        final String url = API_SERVER_URL + "/api/summary";
        final AccountsSummaryResponse expectedAccountsSummaryResponse =
                createSuccessfulAccountsSummaryResponse();

        setUpHttpClientMockForApi(
                url, expectedAccountsSummaryResponse.getAccountsSummaryResponseDto());

        // when
        final AccountsSummaryResponse returnedResponse = aktiaApiClient.getAccountsSummary();

        // then
        assertThat(returnedResponse).isEqualTo(expectedAccountsSummaryResponse);
    }

    @Test
    public void shouldGetTransactionsAndLockedEventsWithContinuationKey() {
        // given
        final String url =
                API_SERVER_URL
                        + String.format("/api/account/%s/transactionsAndLockedEvents", ACCOUNT_ID);
        final TransactionsAndLockedEventsResponse expectedTransactionsAndLockedEventsResponse =
                createTransactionsAndLockedEventsResponseWithContinuationKey();

        final RequestBuilder requestBuilderMock =
                setUpHttpClientMockForApi(
                        url,
                        expectedTransactionsAndLockedEventsResponse
                                .getTransactionsAndLockedEventsResponseDto());
        when(requestBuilderMock.queryParam("continuationKey", CONTINUATION_KEY))
                .thenReturn(requestBuilderMock);

        // when
        final TransactionsAndLockedEventsResponse returnedResponse =
                aktiaApiClient.getTransactionsAndLockedEvents(ACCOUNT_ID, CONTINUATION_KEY);

        // then
        assertThat(returnedResponse).isEqualTo(expectedTransactionsAndLockedEventsResponse);
        verify(requestBuilderMock).queryParam("continuationKey", CONTINUATION_KEY);
    }

    @Test
    public void shouldGetTransactionsAndLockedEventsWithoutContinuationKey() {
        // given
        final String url =
                API_SERVER_URL
                        + String.format("/api/account/%s/transactionsAndLockedEvents", ACCOUNT_ID);
        final TransactionsAndLockedEventsResponse expectedTransactionsAndLockedEventsResponse =
                createTransactionsAndLockedEventsResponseWithoutContinuationKey();

        final RequestBuilder requestBuilderMock =
                setUpHttpClientMockForApi(
                        url,
                        expectedTransactionsAndLockedEventsResponse
                                .getTransactionsAndLockedEventsResponseDto());

        // when
        final TransactionsAndLockedEventsResponse returnedResponse =
                aktiaApiClient.getTransactionsAndLockedEvents(ACCOUNT_ID, null);

        // then
        assertThat(returnedResponse).isEqualTo(expectedTransactionsAndLockedEventsResponse);
        verify(requestBuilderMock, never()).queryParam(anyString(), anyString());
    }

    @Test
    public void shouldThrowExceptionWhenAccessTokenIsNotFound() {
        // given
        when(tokenStorageMock.getToken()).thenReturn(Optional.empty());

        // when
        final Throwable thrown = catchThrowable(aktiaApiClient::getAccountsSummary);

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Access token not found in storage.");
    }

    private RequestBuilder setUpHttpClientMockForAuth(TokenResponseDto response) {
        final RequestBuilder requestBuilderMock = mock(RequestBuilder.class);

        when(requestBuilderMock.header(
                        HttpHeaders.AUTHORIZATION, "Basic " + BASIC_AUTH_HEADER_VALUE))
                .thenReturn(requestBuilderMock);
        when(requestBuilderMock.post(TokenResponseDto.class)).thenReturn(response);

        when(httpClientMock.request(new URL(AUTH_TOKEN_URL))).thenReturn(requestBuilderMock);

        return requestBuilderMock;
    }

    private void setupHttpClientMockForAuthException() {
        final RequestBuilder requestBuilderMock = mock(RequestBuilder.class);
        when(requestBuilderMock.header(
                        HttpHeaders.AUTHORIZATION, "Basic " + BASIC_AUTH_HEADER_VALUE))
                .thenReturn(requestBuilderMock);

        HttpResponse httpResponse = mock(HttpResponse.class);

        when(httpResponse.getStatus()).thenReturn(400);
        when(httpResponse.getBody(String.class))
                .thenReturn(
                        "{\n"
                                + "  \"error_description\": \"Resource owner authentication failed\",\n"
                                + "  \"error\": \"invalid_grant\"\n"
                                + "}");
        when(httpClientMock.request(new URL(AUTH_TOKEN_URL)))
                .thenThrow(new HttpResponseException(null, httpResponse));
    }

    private RequestBuilder setUpHttpClientMockForApi(String urlString, Object response) {
        final RequestBuilder requestBuilderMock = mock(RequestBuilder.class);

        when(requestBuilderMock.addBearerToken(any(OAuth2Token.class)))
                .thenReturn(requestBuilderMock);

        when(requestBuilderMock.get(any())).thenReturn(response);

        when(httpClientMock.request(new URL(urlString))).thenReturn(requestBuilderMock);

        return requestBuilderMock;
    }

    private RequestBuilder setUpHttpClientMockForAuthWithOtp(
            OtpAuthenticationResponseDto response) {
        final RequestBuilder requestBuilderMock = mock(RequestBuilder.class);

        when(requestBuilderMock.addBearerToken(any(OAuth2Token.class)))
                .thenReturn(requestBuilderMock);
        when(requestBuilderMock.post(OtpAuthenticationResponseDto.class)).thenReturn(response);

        final String url = API_SERVER_URL + "/api/login/otp/authenticate";
        when(httpClientMock.request(new URL(url))).thenReturn(requestBuilderMock);

        return requestBuilderMock;
    }

    private static AktiaConfiguration createAktiaConfigurationMock() {
        final AktiaConfiguration aktiaConfigurationMock = mock(AktiaConfiguration.class);
        when(aktiaConfigurationMock.getBasicAuthHeaderValue()).thenReturn(BASIC_AUTH_HEADER_VALUE);

        return aktiaConfigurationMock;
    }
}
