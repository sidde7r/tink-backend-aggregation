package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclTestFixtures.AUTH_CODE;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclTestFixtures.DATE;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclTestFixtures.DIGEST;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclTestFixtures.REFRESH_TOKEN;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclTestFixtures.RESOURCE_ID;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclTestFixtures.SERVER_URL;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclTestFixtures.SIGNATURE;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclTestFixtures.createAccountsResponseDto;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclTestFixtures.createAgentConfigurationMock;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclTestFixtures.createEndUserIdentityResponseDto;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclTestFixtures.createOAuth2Token;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclTestFixtures.createRefreshTokenRequestString;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclTestFixtures.createRetrieveTokenRequestString;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclTestFixtures.createTokenResponseDto;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclTestFixtures.createTransactionsResponseDto;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.FrTransferDestinationFetcherTestFixtures.createTrustedBeneficiariesPage1Response;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.FrTransferDestinationFetcherTestFixtures.createTrustedBeneficiariesPage2Response;

import java.util.Optional;
import javax.ws.rs.core.MediaType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.accesstoken.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.accesstoken.RetrieveTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.accesstoken.TokenResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.account.AccountsResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.identity.EndUserIdentityResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.transaction.TransactionsResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.configuration.LclConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.dto.TrustedBeneficiariesResponseDto;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2TokenStorage;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;

public class LclApiClientTest {

    private static final String BASE_URL = SERVER_URL + "/aisp";
    private static final String TOKEN_URL = SERVER_URL + "/token";
    private static final String ACCOUNTS_URL = BASE_URL + "/accounts";
    private static final String TRANSACTIONS_URL_TEMPLATE =
            ACCOUNTS_URL + "/" + RESOURCE_ID + "/transactions?page=";
    private static final String TRANSACTIONS_PAGE_1_URL = TRANSACTIONS_URL_TEMPLATE + "1";
    private static final String TRANSACTIONS_PAGE_2_URL = TRANSACTIONS_URL_TEMPLATE + "2";
    private static final String END_USER_IDENTITY_URL = BASE_URL + "/end-user-identity";
    private static final String TRUSTED_BENEFICIARIES_URL = BASE_URL + "/trusted-beneficiaries";
    private static final String TRUSTED_BENEFICIARIES_NEXT_PAGE_URL =
            BASE_URL + "/trusted-beneficiaries?page=2";

    private LclApiClient lclApiClient;

    private TinkHttpClient httpClientMock;

    @Before
    public void setUp() {
        final AgentConfiguration<LclConfiguration> agentConfigurationMock =
                createAgentConfigurationMock();
        final OAuth2TokenStorage tokenStorageMock = createOAuth2TokenStorageMock();
        final LclHeaderValueProvider headerValueProvider = createLclHeaderValueProviderMock();

        httpClientMock = mock(TinkHttpClient.class);
        lclApiClient =
                new LclApiClient(
                        httpClientMock,
                        headerValueProvider,
                        tokenStorageMock,
                        agentConfigurationMock);
    }

    @Test
    public void shouldRetrieveAccessToken() {
        // given
        final TokenResponseDto expectedResult = createTokenResponseDto();
        final RequestBuilder requestBuilderMock = setUpHttpClientMockForAuth();
        final ArgumentCaptor<RetrieveTokenRequest> retrieveTokenRequestArgumentCaptor =
                ArgumentCaptor.forClass(RetrieveTokenRequest.class);

        when(requestBuilderMock.post(
                        eq(TokenResponseDto.class), retrieveTokenRequestArgumentCaptor.capture()))
                .thenReturn(expectedResult);

        // when
        final TokenResponseDto returnedResult = lclApiClient.retrieveAccessToken(AUTH_CODE);

        // then
        assertThat(returnedResult).isEqualTo(expectedResult);

        final String expectedRequest = createRetrieveTokenRequestString();
        final String actualRequest = retrieveTokenRequestArgumentCaptor.getValue().getBodyValue();

        assertThat(actualRequest).isEqualTo(expectedRequest);
    }

    @Test
    public void shouldRefreshAccessToken() {
        // given
        final TokenResponseDto expectedResult = createTokenResponseDto();
        final RequestBuilder requestBuilderMock = setUpHttpClientMockForAuth();
        final ArgumentCaptor<RefreshTokenRequest> refreshTokenRequestArgumentCaptor =
                ArgumentCaptor.forClass(RefreshTokenRequest.class);

        when(requestBuilderMock.post(
                        eq(TokenResponseDto.class), refreshTokenRequestArgumentCaptor.capture()))
                .thenReturn(expectedResult);

        // when
        final Optional<TokenResponseDto> returnedResult =
                lclApiClient.refreshAccessToken(REFRESH_TOKEN);

        // then
        assertThat(returnedResult.isPresent()).isTrue();
        returnedResult.ifPresent(
                tokenResponse -> assertThat(tokenResponse).isEqualTo(expectedResult));

        final String expectedRequest = createRefreshTokenRequestString();
        final String actualRequest = refreshTokenRequestArgumentCaptor.getValue().getBodyValue();

        assertThat(actualRequest).isEqualTo(expectedRequest);
    }

    @Test
    public void shouldReturnEmptyOptionalStringWhenRefreshFails() {
        // given
        final RequestBuilder requestBuilderMock = setUpHttpClientMockForAuth();
        final String errorMessage = "error";
        final BankServiceException exceptionMock = mock(BankServiceException.class);
        when(exceptionMock.getMessage()).thenReturn(errorMessage);

        when(requestBuilderMock.post(any(), any())).thenThrow(exceptionMock);

        // when
        final Optional<TokenResponseDto> returnedResult =
                lclApiClient.refreshAccessToken(REFRESH_TOKEN);

        // then
        assertThat(returnedResult.isPresent()).isFalse();
    }

    @Test
    public void shouldGetAccountsResponse() {
        // given
        final AccountsResponseDto expectedResult = createAccountsResponseDto();
        setUpHttpClientMockForApi(ACCOUNTS_URL, expectedResult);

        // when
        final AccountsResponseDto returnedResult = lclApiClient.getAccountsResponse();

        // then
        assertThat(returnedResult).isEqualTo(expectedResult);
    }

    @Test
    public void shouldGetTransactionsResponseForPage1() {
        // given
        final TransactionsResponseDto expectedResult = createTransactionsResponseDto();
        setUpHttpClientMockForApi(TRANSACTIONS_PAGE_1_URL, expectedResult);

        // when
        final TransactionsResponseDto returnedResult =
                lclApiClient.getTransactionsResponse(RESOURCE_ID, 1);

        // then
        assertThat(returnedResult).isEqualTo(expectedResult);
    }

    @Test
    public void shouldGetTransactionsResponseForPage2() {
        // given
        final TransactionsResponseDto expectedResult = createTransactionsResponseDto();
        setUpHttpClientMockForApi(TRANSACTIONS_PAGE_2_URL, expectedResult);

        // when
        final TransactionsResponseDto returnedResult =
                lclApiClient.getTransactionsResponse(RESOURCE_ID, 2);

        // then
        assertThat(returnedResult).isEqualTo(expectedResult);
    }

    @Test
    public void shouldGetEndUserIdentity() {
        // given
        final EndUserIdentityResponseDto expectedResult = createEndUserIdentityResponseDto();
        setUpHttpClientMockForApi(END_USER_IDENTITY_URL, expectedResult);

        // when
        final EndUserIdentityResponseDto returnedResult = lclApiClient.getEndUserIdentity();

        // then
        assertThat(returnedResult).isEqualTo(expectedResult);
    }

    @Test
    public void shouldGetTrustedBeneficiaries() {
        // given
        final TrustedBeneficiariesResponseDto expectedResult =
                createTrustedBeneficiariesPage1Response();

        setUpHttpClientMockForApi(TRUSTED_BENEFICIARIES_URL, expectedResult);

        // when
        final Optional<TrustedBeneficiariesResponseDto> returnedResult =
                lclApiClient.getTrustedBeneficiaries();

        // then
        assertThat(returnedResult.isPresent()).isTrue();
        returnedResult.ifPresent(
                actualResponse -> assertThat(actualResponse).isEqualTo(expectedResult));
    }

    @Test
    public void shouldGetTrustedBeneficiariesNextPage() {
        // given
        final TrustedBeneficiariesResponseDto expectedResult =
                createTrustedBeneficiariesPage2Response();

        setUpHttpClientMockForApi(TRUSTED_BENEFICIARIES_NEXT_PAGE_URL, expectedResult);

        // when
        final Optional<TrustedBeneficiariesResponseDto> returnedResult =
                lclApiClient.getTrustedBeneficiaries(TRUSTED_BENEFICIARIES_NEXT_PAGE_URL);

        // then
        assertThat(returnedResult.isPresent()).isTrue();
        returnedResult.ifPresent(
                actualResponse -> assertThat(actualResponse).isEqualTo(expectedResult));
    }

    private void setUpHttpClientMockForApi(String urlString, Object response) {
        final RequestBuilder requestBuilderMock = mock(RequestBuilder.class);

        when(requestBuilderMock.addBearerToken(any(OAuth2Token.class)))
                .thenReturn(requestBuilderMock);

        when(requestBuilderMock.header(eq(Psd2Headers.Keys.X_REQUEST_ID), anyString()))
                .thenReturn(requestBuilderMock);
        when(requestBuilderMock.header(eq(Psd2Headers.Keys.DATE), eq(DATE)))
                .thenReturn(requestBuilderMock);
        when(requestBuilderMock.header(Psd2Headers.Keys.DIGEST, DIGEST))
                .thenReturn(requestBuilderMock);
        when(requestBuilderMock.header(Psd2Headers.Keys.SIGNATURE, SIGNATURE))
                .thenReturn(requestBuilderMock);

        when(requestBuilderMock.get(any())).thenReturn(response);

        when(httpClientMock.request(urlString)).thenReturn(requestBuilderMock);
    }

    private RequestBuilder setUpHttpClientMockForAuth() {
        final RequestBuilder requestBuilderMock = mock(RequestBuilder.class);

        when(requestBuilderMock.addBearerToken(any(OAuth2Token.class)))
                .thenReturn(requestBuilderMock);

        when(requestBuilderMock.header(eq(Psd2Headers.Keys.X_REQUEST_ID), anyString()))
                .thenReturn(requestBuilderMock);
        when(requestBuilderMock.header(eq(Psd2Headers.Keys.DATE), eq(DATE)))
                .thenReturn(requestBuilderMock);
        when(requestBuilderMock.header(Psd2Headers.Keys.DIGEST, DIGEST))
                .thenReturn(requestBuilderMock);
        when(requestBuilderMock.header(Psd2Headers.Keys.SIGNATURE, SIGNATURE))
                .thenReturn(requestBuilderMock);
        when(requestBuilderMock.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE))
                .thenReturn(requestBuilderMock);

        when(httpClientMock.request(TOKEN_URL)).thenReturn(requestBuilderMock);

        return requestBuilderMock;
    }

    private static OAuth2TokenStorage createOAuth2TokenStorageMock() {
        final OAuth2TokenStorage tokenStorageMock = mock(OAuth2TokenStorage.class);
        final OAuth2Token oAuth2Token = createOAuth2Token();

        when(tokenStorageMock.getToken()).thenReturn(Optional.of(oAuth2Token));

        return tokenStorageMock;
    }

    private static LclHeaderValueProvider createLclHeaderValueProviderMock() {
        final LclHeaderValueProvider headerValueProviderMock = mock(LclHeaderValueProvider.class);

        when(headerValueProviderMock.getDateHeaderValue()).thenReturn(DATE);
        when(headerValueProviderMock.getDigestHeaderValue(any())).thenReturn(DIGEST);
        when(headerValueProviderMock.getSignatureHeaderValue(anyString(), anyString(), anyString()))
                .thenReturn(SIGNATURE);

        return headerValueProviderMock;
    }
}
