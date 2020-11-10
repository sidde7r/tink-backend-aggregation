package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.ACCESS_TOKEN_1;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.AUTH_CODE_1;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.AUTH_MAC_VALUE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.CLIENT_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.DATA_MAC_VALUE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.REDIRECT_URL;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.SERVER_URL;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.VALID_REFRESH_TOKEN;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.createAccessTokenResponse;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.createAccountsResponse;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.createBalancesRawResponse;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.createHmacToken;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.createRevokeTokenResponse;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.createTransactionsRawResponse;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.getRefreshRequest;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.getRevokeRequest;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.getTokenRequest;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestValidators.getAmexRequestIdMatchingRegex;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.core.HttpHeaders;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmericanExpressConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexGrantType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.configuration.AmexConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.AccountsResponseDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.BalanceDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.TransactionsResponseDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.token.RefreshRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.token.RevokeRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.token.RevokeResponseDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.token.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.token.TokenResponseDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.macgenerator.AmexMacGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.hmac.HmacMultiTokenStorage;
import se.tink.backend.aggregation.nxgen.core.authentication.HmacToken;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class AmexApiClientTest {

    private static final String RETRIEVE_TOKEN_URL = SERVER_URL + "/apiplatform/v2/oauth/token/mac";
    private static final String REFRESH_TOKEN_URL =
            SERVER_URL + "/apiplatform/v1/oauth/token/refresh/mac";
    private static final String REVOKE_TOKEN_URL =
            SERVER_URL + "/apiplatform/v2/oauth/token_revocation/mac";
    private static final String BASE_URL = SERVER_URL + "/servicing/v1";

    private AmexApiClient amexApiClient;

    private TinkHttpClient httpClientMock;

    @Before
    public void setUp() {
        final AmexConfiguration amexConfigurationMock = mock(AmexConfiguration.class);
        when(amexConfigurationMock.getClientId()).thenReturn(CLIENT_ID);

        final AmexMacGenerator amexMacGeneratorMock = mock(AmexMacGenerator.class);
        when(amexMacGeneratorMock.generateAuthMacValue(AmexGrantType.AUTHORIZATION_CODE))
                .thenReturn(AUTH_MAC_VALUE);
        when(amexMacGeneratorMock.generateAuthMacValue(AmexGrantType.REFRESH_TOKEN))
                .thenReturn(AUTH_MAC_VALUE);
        when(amexMacGeneratorMock.generateAuthMacValue(AmexGrantType.REVOKE))
                .thenReturn(AUTH_MAC_VALUE);
        when(amexMacGeneratorMock.generateDataMacValue(anyString(), any()))
                .thenReturn(DATA_MAC_VALUE);

        httpClientMock = mock(TinkHttpClient.class);

        TemporaryStorage temporaryStorage = mock(TemporaryStorage.class);

        HmacMultiTokenStorage hmacMultiTokenStorage = mock(HmacMultiTokenStorage.class);

        amexApiClient =
                new AmexApiClient(
                        amexConfigurationMock,
                        REDIRECT_URL,
                        httpClientMock,
                        amexMacGeneratorMock,
                        new ObjectMapper(),
                        temporaryStorage,
                        hmacMultiTokenStorage);
    }

    @Test
    public void shouldRetrieveAccessToken() {
        // given
        final TokenResponseDto expectedTokenResponse = createAccessTokenResponse();
        final RequestBuilder requestBuilderMock =
                setUpHttpClientMockForAuth(RETRIEVE_TOKEN_URL, expectedTokenResponse);
        final ArgumentCaptor<TokenRequest> tokenRequestCaptor =
                ArgumentCaptor.forClass(TokenRequest.class);
        when(requestBuilderMock.body(tokenRequestCaptor.capture(), anyString()))
                .thenReturn(requestBuilderMock);

        // when
        final TokenResponseDto returnedResponse = amexApiClient.retrieveAccessToken(AUTH_CODE_1);

        // then
        assertThat(returnedResponse).isEqualTo(expectedTokenResponse);

        final String expectedTokenRequest = getTokenRequest();
        final String actualTokenRequest = tokenRequestCaptor.getValue().getBodyValue();
        assertThat(actualTokenRequest).isEqualTo(expectedTokenRequest);
    }

    @Test
    public void shouldRefreshAccessToken() {
        // given
        final TokenResponseDto expectedTokenResponse = createAccessTokenResponse();
        final RequestBuilder requestBuilderMock =
                setUpHttpClientMockForAuth(REFRESH_TOKEN_URL, expectedTokenResponse);
        final ArgumentCaptor<RefreshRequest> refreshRequestCaptor =
                ArgumentCaptor.forClass(RefreshRequest.class);
        when(requestBuilderMock.body(refreshRequestCaptor.capture(), anyString()))
                .thenReturn(requestBuilderMock);

        // when
        final Optional<TokenResponseDto> maybeResponse =
                amexApiClient.refreshAccessToken(VALID_REFRESH_TOKEN);

        // then
        assertThat(maybeResponse.isPresent()).isTrue();

        final TokenResponseDto returnedResponse = maybeResponse.get();
        assertThat(returnedResponse).isEqualTo(expectedTokenResponse);

        final String expectedRefreshRequest = getRefreshRequest();
        final String actualRefreshRequest = refreshRequestCaptor.getValue().getBodyValue();
        assertThat(actualRefreshRequest).isEqualTo(expectedRefreshRequest);
    }

    @Test
    public void shouldReturnEmptyOptionalStringWhenRefreshFails() {
        // given
        final RequestBuilder requestBuilderMock =
                setUpHttpClientMockForAuth(REFRESH_TOKEN_URL, null);
        final String errorMessage = "error";
        final SessionException exceptionMock = mock(SessionException.class);
        when(exceptionMock.getMessage()).thenReturn(errorMessage);

        when(requestBuilderMock.post(any())).thenThrow(exceptionMock);
        final ArgumentCaptor<RefreshRequest> refreshRequestCaptor =
                ArgumentCaptor.forClass(RefreshRequest.class);
        when(requestBuilderMock.body(refreshRequestCaptor.capture(), anyString()))
                .thenReturn(requestBuilderMock);

        // when
        final Optional<TokenResponseDto> maybeResponse =
                amexApiClient.refreshAccessToken(VALID_REFRESH_TOKEN);

        // then
        assertThat(maybeResponse.isPresent()).isFalse();

        final String expectedRefreshRequest = getRefreshRequest();
        final String actualRefreshRequest = refreshRequestCaptor.getValue().getBodyValue();
        assertThat(actualRefreshRequest).isEqualTo(expectedRefreshRequest);
    }

    @Test
    public void shouldRevokeAccessToken() {
        // given
        final RevokeResponseDto expectedRevokeResponse = createRevokeTokenResponse();
        final RequestBuilder requestBuilderMock =
                setUpHttpClientMockForAuth(REVOKE_TOKEN_URL, expectedRevokeResponse);
        final ArgumentCaptor<RevokeRequest> revokeRequestCaptor =
                ArgumentCaptor.forClass(RevokeRequest.class);
        when(requestBuilderMock.body(revokeRequestCaptor.capture(), anyString()))
                .thenReturn(requestBuilderMock);

        // when
        final RevokeResponseDto returnedResponse = amexApiClient.revokeAccessToken(ACCESS_TOKEN_1);

        // then
        assertThat(returnedResponse).isEqualTo(expectedRevokeResponse);

        final String expectedRevokeRequest = getRevokeRequest();
        final String actualRevokeRequest = revokeRequestCaptor.getValue().getBodyValue();
        assertThat(actualRevokeRequest).isEqualTo(expectedRevokeRequest);
    }

    @Test
    public void shouldFetchAccounts() {
        // given
        final String url = BASE_URL + "/member/accounts";
        final AccountsResponseDto expectedResponse = createAccountsResponse();
        final HmacToken hmacToken = createHmacToken();

        setUpHttpClientMockForApi(url, expectedResponse);

        // when
        final AccountsResponseDto actualResponse = amexApiClient.fetchAccounts(hmacToken);

        // then
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    public void shouldFetchBalances() {
        // given
        final String url = BASE_URL + "/financials/balances";
        final List<LinkedHashMap<String, String>> expectedResponseMap = createBalancesRawResponse();
        final List<BalanceDto> expectedResponse =
                new ObjectMapper()
                        .convertValue(
                                expectedResponseMap, new TypeReference<List<BalanceDto>>() {});
        final HmacToken hmacToken = createHmacToken();

        setUpHttpClientMockForApi(url, expectedResponseMap);

        // when
        final List<BalanceDto> actualResponse = amexApiClient.fetchBalances(hmacToken);

        // then
        assertThat(actualResponse).containsExactlyElementsOf(expectedResponse);
    }

    @Test
    public void shouldFetchTransactions() {
        // given
        final String url = BASE_URL + "/financials/transactions";
        final List<LinkedHashMap<String, String>> expectedResponseMap =
                createTransactionsRawResponse();
        final List<TransactionsResponseDto> expectedResponse =
                new ObjectMapper()
                        .convertValue(
                                expectedResponseMap,
                                new TypeReference<List<TransactionsResponseDto>>() {});

        final HmacToken hmacToken = createHmacToken();

        setUpHttpClientMockForApiTransactions(url, expectedResponseMap);

        // when
        final List<TransactionsResponseDto> actualResponse =
                amexApiClient.fetchTransactions(hmacToken, new Date(), new Date());

        // then
        assertThat(actualResponse.stream().findFirst().get())
                .isEqualTo(expectedResponse.stream().findFirst().get());
    }

    private RequestBuilder setUpHttpClientMockForAuth(String urlString, Object response) {
        final RequestBuilder requestBuilderMock = mock(RequestBuilder.class);

        when(requestBuilderMock.header(AmericanExpressConstants.Headers.X_AMEX_API_KEY, CLIENT_ID))
                .thenReturn(requestBuilderMock);
        when(requestBuilderMock.header(
                        AmericanExpressConstants.Headers.AUTHENTICATION, AUTH_MAC_VALUE))
                .thenReturn(requestBuilderMock);
        when(requestBuilderMock.post(any())).thenReturn(response);

        when(httpClientMock.request(new URL(urlString))).thenReturn(requestBuilderMock);

        return requestBuilderMock;
    }

    private void setUpHttpClientMockForApi(String urlString, Object response) {
        final RequestBuilder requestBuilderMock = mock(RequestBuilder.class);

        when(requestBuilderMock.header(anyString(), anyString())).thenReturn(requestBuilderMock);

        when(requestBuilderMock.header(AmericanExpressConstants.Headers.X_AMEX_API_KEY, CLIENT_ID))
                .thenReturn(requestBuilderMock);
        when(requestBuilderMock.header(HttpHeaders.AUTHORIZATION, DATA_MAC_VALUE))
                .thenReturn(requestBuilderMock);
        when(requestBuilderMock.header(
                        eq(AmericanExpressConstants.Headers.X_AMEX_REQUEST_ID),
                        matches(getAmexRequestIdMatchingRegex())))
                .thenReturn(requestBuilderMock);
        when(requestBuilderMock.get(any())).thenReturn(response);

        when(httpClientMock.request(new URL(urlString))).thenReturn(requestBuilderMock);
    }

    private void setUpHttpClientMockForApiTransactions(String urlString, Object response) {
        final RequestBuilder requestBuilderMock = mock(RequestBuilder.class);

        when(requestBuilderMock.header(anyString(), anyString())).thenReturn(requestBuilderMock);

        when(requestBuilderMock.header(AmericanExpressConstants.Headers.X_AMEX_API_KEY, CLIENT_ID))
                .thenReturn(requestBuilderMock);
        when(requestBuilderMock.header(HttpHeaders.AUTHORIZATION, DATA_MAC_VALUE))
                .thenReturn(requestBuilderMock);
        when(requestBuilderMock.header(
                        eq(AmericanExpressConstants.Headers.X_AMEX_REQUEST_ID),
                        matches(getAmexRequestIdMatchingRegex())))
                .thenReturn(requestBuilderMock);
        when(requestBuilderMock.get(any())).thenReturn(response);

        when(httpClientMock.request(
                        new URL(urlString)
                                .queryParam(
                                        "start_date",
                                        ThreadSafeDateFormat.FORMATTER_DAILY.format(new Date()))
                                .queryParam(
                                        "end_date",
                                        ThreadSafeDateFormat.FORMATTER_DAILY.format(new Date()))
                                .queryParam("limit", "1000")
                                .queryParam("status", "posted")))
                .thenReturn(requestBuilderMock);

        when(httpClientMock.request(
                        new URL(urlString)
                                .queryParam(
                                        "start_date",
                                        ThreadSafeDateFormat.FORMATTER_DAILY.format(new Date()))
                                .queryParam(
                                        "end_date",
                                        ThreadSafeDateFormat.FORMATTER_DAILY.format(new Date()))
                                .queryParam("limit", "1000")
                                .queryParam("status", "pending")))
                .thenReturn(requestBuilderMock);
    }
}
