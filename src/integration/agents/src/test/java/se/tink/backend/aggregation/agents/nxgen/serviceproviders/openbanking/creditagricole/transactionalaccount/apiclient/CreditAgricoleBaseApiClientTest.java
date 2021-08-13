package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.apiclient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleTestFixtures.ACCESS_TOKEN;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleTestFixtures.ACCOUNT_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleTestFixtures.AUTH_CODE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleTestFixtures.BASE_URL;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleTestFixtures.CLIENT_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleTestFixtures.DATE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleTestFixtures.PSU_IP_ADDR;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleTestFixtures.REDIRECT_URL;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleTestFixtures.REFRESH_TOKEN;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleTestFixtures.STRING_DATE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleTestFixtures.createTokenResponse;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleTestFixtures.createTransactionsResponse;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.FrTransferDestinationFetcherTestFixtures.BENEFICIARIES_2ND_PAGE_PATH;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.configuration.CreditAgricoleBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.configuration.CreditAgricoleBranchConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities.AccountIdEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc.PutConsentsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.dto.TrustedBeneficiariesResponseDto;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class CreditAgricoleBaseApiClientTest {

    private static final String AUTH_URL = BASE_URL + "/authentication/v1/openid/token";
    private static final String ACCOUNTS_URL = BASE_URL + "/dsp2/v1/accounts";
    private static final String CONSENT_URL = BASE_URL + "/dsp2/v1/consents";
    private static final String BENEFICIARIES_URL = BASE_URL + "/dsp2/v1/trusted-beneficiaries";
    private static final String TRANSACTIONS_URL =
            BASE_URL
                    + "/dsp2/v1/accounts/"
                    + ACCOUNT_ID
                    + "/transactions?dateFrom="
                    + STRING_DATE
                    + "&dateTo="
                    + STRING_DATE;
    private static final String UUID_4_MATCHING_REGEX =
            "[0-9a-f]{8}\\-[0-9a-f]{4}\\-4[0-9a-f]{3}\\-[89ab][0-9a-f]{3}\\-[0-9a-f]{12}";

    private CreditAgricoleBaseApiClient apiClient;
    private TinkHttpClient httpClientMock;
    private CreditAgricoleStorage creditAgricoleStorageMock;

    @Before
    public void setUp() throws Exception {
        final AgentConfiguration<CreditAgricoleBaseConfiguration>
                creditAgricoleBaseConfigurationMock = getConfigurationMock();
        final CreditAgricoleBranchConfiguration creditAgricoleBranchConfigurationMock =
                getBranchConfigurationMock();

        httpClientMock = mock(TinkHttpClient.class);
        creditAgricoleStorageMock = createCreditAgricoleStorageMock();

        apiClient =
                new CreditAgricoleBaseApiClient(
                        httpClientMock,
                        creditAgricoleStorageMock,
                        creditAgricoleBaseConfigurationMock,
                        creditAgricoleBranchConfigurationMock,
                        PSU_IP_ADDR);
    }

    @Test
    public void shouldGetToken() {
        // given
        final TokenResponse expectedResponse = createTokenResponse();
        final RequestBuilder requestBuilderMock = setUpHttpClientMockForAuth();
        final ArgumentCaptor<String> tokenRequestStringCaptor =
                ArgumentCaptor.forClass(String.class);
        when(requestBuilderMock.post(eq(TokenResponse.class), tokenRequestStringCaptor.capture()))
                .thenReturn(expectedResponse);

        // when
        final TokenResponse returnedResponse = apiClient.getToken(AUTH_CODE);

        // then
        assertThat(returnedResponse).isEqualTo(expectedResponse);

        final String requestString = tokenRequestStringCaptor.getValue();

        assertThat(requestString)
                .isEqualTo(
                        "scope=aisp+extended_transaction_history&grant_type=authorization_code&code=DUMMY_AUTH_CODE&redirect_uri=http%3A%2F%2Fredirect-url&client_id=DUMMY_CLIENT_ID");
        verify(creditAgricoleStorageMock).storeInitialFetchState(Boolean.TRUE);
    }

    @Test
    public void shouldRefreshToken() {
        // given
        final TokenResponse expectedResponse = createTokenResponse();
        final RequestBuilder requestBuilderMock = setUpHttpClientMockForAuth();
        final ArgumentCaptor<String> tokenRequestStringCaptor =
                ArgumentCaptor.forClass(String.class);
        when(requestBuilderMock.post(eq(TokenResponse.class), tokenRequestStringCaptor.capture()))
                .thenReturn(expectedResponse);

        // when
        final OAuth2Token returnedResponse = apiClient.refreshToken(REFRESH_TOKEN);

        // then
        assertThat(returnedResponse.getAccessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(returnedResponse.getRefreshToken().isPresent()).isTrue();
        assertThat(returnedResponse.getRefreshToken().get()).isEqualTo(REFRESH_TOKEN);

        final String requestString = tokenRequestStringCaptor.getValue();

        assertThat(requestString)
                .isEqualTo(
                        "scope=aisp+extended_transaction_history&grant_type=refresh_token&refresh_token=DUMMY_REFRESH_TOKEN&redirect_uri=http%3A%2F%2Fredirect-url&client_id=DUMMY_CLIENT_ID");
        verify(creditAgricoleStorageMock).storeInitialFetchState(Boolean.FALSE);
        verify(creditAgricoleStorageMock).storeToken(returnedResponse);
    }

    @Test
    public void shouldGetAccounts() {
        // given
        final GetAccountsResponse expectedResponse = mock(GetAccountsResponse.class);

        setUpHttpClientMockForApi(ACCOUNTS_URL, expectedResponse);

        // when
        final GetAccountsResponse returnedResponse = apiClient.getAccounts();

        // then
        assertThat(returnedResponse).isEqualTo(expectedResponse);
    }

    @Test
    public void ifFetchTransactionsReturns204_EmptyResponseIsReturned() {
        // given
        HttpResponse noContentResponse = mock(HttpResponse.class);
        when(noContentResponse.getStatus()).thenReturn(204);

        setUpHttpClientMockForTransactionsFetch(new URL(TRANSACTIONS_URL), noContentResponse);

        // when
        GetTransactionsResponse transactions = apiClient.getTransactions(ACCOUNT_ID, DATE, DATE);

        // then
        assertThat(transactions.getTinkTransactions()).isEmpty();
    }

    @Test
    public void ifFetchTransactionsReturns200_bodyIsExtracted() {
        // given
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatus()).thenReturn(200);

        final GetTransactionsResponse expectedResponse = createTransactionsResponse();
        when(response.getBody(GetTransactionsResponse.class)).thenReturn(expectedResponse);

        setUpHttpClientMockForTransactionsFetch(new URL(TRANSACTIONS_URL), response);

        // when
        GetTransactionsResponse transactions = apiClient.getTransactions(ACCOUNT_ID, DATE, DATE);

        // then
        assertThat(transactions).isEqualTo(expectedResponse);
        assertThat(transactions.getTinkTransactions()).isNotEmpty();
    }

    @Test
    public void shouldPutConsents() {
        // given
        final RequestBuilder requestBuilder = setUpHttpClientMockForPutConsent();
        final List<AccountIdEntity> listOfNecessaryConsents =
                Collections.singletonList(mock(AccountIdEntity.class));

        // when
        apiClient.putConsents(listOfNecessaryConsents);

        // then
        final PutConsentsRequest expectedConsentsRequest =
                new PutConsentsRequest(
                        listOfNecessaryConsents, listOfNecessaryConsents, true, true);
        verify(requestBuilder).put(expectedConsentsRequest);
    }

    @Test
    public void shouldGetBeneficiariesFirstPage() {
        // given
        final HttpResponse httpResponseMock = mock(HttpResponse.class);
        final TrustedBeneficiariesResponseDto expectedResponse =
                mock(TrustedBeneficiariesResponseDto.class);
        when(httpResponseMock.getBody(TrustedBeneficiariesResponseDto.class))
                .thenReturn(expectedResponse);
        when(httpResponseMock.getStatus()).thenReturn(200);

        setUpHttpClientMockForApi(BENEFICIARIES_URL, httpResponseMock);

        // when
        final Optional<TrustedBeneficiariesResponseDto> returnedResponse =
                apiClient.getTrustedBeneficiaries();

        // then
        assertThat(returnedResponse.isPresent()).isTrue();
        assertThat(returnedResponse.get()).isEqualTo(expectedResponse);
    }

    @Test
    public void shouldGetBeneficiariesSecondPage() {
        // given
        final HttpResponse httpResponseMock = mock(HttpResponse.class);
        final TrustedBeneficiariesResponseDto expectedResponse =
                mock(TrustedBeneficiariesResponseDto.class);
        when(httpResponseMock.getBody(TrustedBeneficiariesResponseDto.class))
                .thenReturn(expectedResponse);
        when(httpResponseMock.getStatus()).thenReturn(200);

        final String url = BASE_URL + "/dsp2/v1" + BENEFICIARIES_2ND_PAGE_PATH;
        setUpHttpClientMockForApi(url, httpResponseMock);

        // when
        final Optional<TrustedBeneficiariesResponseDto> returnedResponse =
                apiClient.getTrustedBeneficiaries(BENEFICIARIES_2ND_PAGE_PATH);

        // then
        assertThat(returnedResponse.isPresent()).isTrue();
        assertThat(returnedResponse.get()).isEqualTo(expectedResponse);
    }

    @Test
    public void shouldReturnEmptyWhenNoTrustedBeneficiariesExist() {
        // given
        final HttpResponse httpResponseMock = mock(HttpResponse.class);
        when(httpResponseMock.getStatus()).thenReturn(204);

        setUpHttpClientMockForApi(BENEFICIARIES_URL, httpResponseMock);

        // when
        final Optional<TrustedBeneficiariesResponseDto> returnedResponse =
                apiClient.getTrustedBeneficiaries();

        // then
        assertThat(returnedResponse.isPresent()).isFalse();
    }

    private RequestBuilder setUpHttpClientMockForAuth() {
        final RequestBuilder requestBuilderMock = mock(RequestBuilder.class);

        when(requestBuilderMock.header(
                        HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED))
                .thenReturn(requestBuilderMock);
        when(requestBuilderMock.header(
                        eq(CreditAgricoleBaseConstants.HeaderKeys.CORRELATION_ID),
                        matches(UUID_4_MATCHING_REGEX)))
                .thenReturn(requestBuilderMock);
        when(requestBuilderMock.header(
                        CreditAgricoleBaseConstants.HeaderKeys.CATS_CONSOMMATEUR,
                        CreditAgricoleBaseConstants.HeaderValues.CATS_CONSOMMATEUR))
                .thenReturn(requestBuilderMock);
        when(requestBuilderMock.header(
                        CreditAgricoleBaseConstants.HeaderKeys.CATS_CONSOMMATEURORIGINE,
                        CreditAgricoleBaseConstants.HeaderValues.CATS_CONSOMMATEURORIGINE))
                .thenReturn(requestBuilderMock);
        when(requestBuilderMock.header(
                        CreditAgricoleBaseConstants.HeaderKeys.CATS_CANAL,
                        CreditAgricoleBaseConstants.HeaderValues.CATS_CANAL))
                .thenReturn(requestBuilderMock);

        when(requestBuilderMock.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBuilderMock);

        when(httpClientMock.request(AUTH_URL)).thenReturn(requestBuilderMock);

        return requestBuilderMock;
    }

    private void setUpHttpClientMockForApi(String urlString, Object response) {
        final RequestBuilder requestBuilderMock = mock(RequestBuilder.class);

        when(requestBuilderMock.header(
                        CreditAgricoleBaseConstants.HeaderKeys.AUTHORIZATION,
                        "Bearer " + ACCESS_TOKEN))
                .thenReturn(requestBuilderMock);

        when(requestBuilderMock.header(
                        CreditAgricoleBaseConstants.HeaderKeys.PSU_IP_ADDRESS, PSU_IP_ADDR))
                .thenReturn(requestBuilderMock);

        when(requestBuilderMock.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBuilderMock);

        when(requestBuilderMock.get(any())).thenReturn(response);

        when(httpClientMock.request(new URL(urlString))).thenReturn(requestBuilderMock);
    }

    private void setUpHttpClientMockForTransactionsFetch(URL url, HttpResponse response) {
        final RequestBuilder requestBuilderMock = mock(RequestBuilder.class);

        when(requestBuilderMock.header(
                        CreditAgricoleBaseConstants.HeaderKeys.AUTHORIZATION,
                        "Bearer " + ACCESS_TOKEN))
                .thenReturn(requestBuilderMock);

        when(requestBuilderMock.header(
                        CreditAgricoleBaseConstants.HeaderKeys.PSU_IP_ADDRESS, PSU_IP_ADDR))
                .thenReturn(requestBuilderMock);

        when(requestBuilderMock.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBuilderMock);

        when(requestBuilderMock.get(HttpResponse.class)).thenReturn(response);

        when(httpClientMock.request(url)).thenReturn(requestBuilderMock);
    }

    private RequestBuilder setUpHttpClientMockForPutConsent() {
        final RequestBuilder requestBuilderMock = mock(RequestBuilder.class);

        when(requestBuilderMock.header(
                        CreditAgricoleBaseConstants.HeaderKeys.AUTHORIZATION,
                        "Bearer " + ACCESS_TOKEN))
                .thenReturn(requestBuilderMock);

        when(requestBuilderMock.header(
                        CreditAgricoleBaseConstants.HeaderKeys.PSU_IP_ADDRESS, PSU_IP_ADDR))
                .thenReturn(requestBuilderMock);

        when(requestBuilderMock.type(MediaType.APPLICATION_JSON)).thenReturn(requestBuilderMock);

        when(httpClientMock.request(new URL(CONSENT_URL))).thenReturn(requestBuilderMock);

        return requestBuilderMock;
    }

    private static AgentConfiguration<CreditAgricoleBaseConfiguration> getConfigurationMock() {
        final CreditAgricoleBaseConfiguration creditAgricoleBaseConfigurationMock =
                mock(CreditAgricoleBaseConfiguration.class);

        when(creditAgricoleBaseConfigurationMock.getClientId()).thenReturn(CLIENT_ID);

        AgentConfiguration<CreditAgricoleBaseConfiguration> agentConfiguration =
                mock(AgentConfiguration.class);
        when(agentConfiguration.getProviderSpecificConfiguration())
                .thenReturn(creditAgricoleBaseConfigurationMock);
        when(agentConfiguration.getRedirectUrl()).thenReturn(REDIRECT_URL);

        return agentConfiguration;
    }

    private CreditAgricoleBranchConfiguration getBranchConfigurationMock() {
        final CreditAgricoleBranchConfiguration creditAgricoleBranchConfigurationMock =
                mock(CreditAgricoleBranchConfiguration.class);

        when(creditAgricoleBranchConfigurationMock.getBaseUrl()).thenReturn(BASE_URL);
        return creditAgricoleBranchConfigurationMock;
    }

    private static CreditAgricoleStorage createCreditAgricoleStorageMock() {
        final CreditAgricoleStorage creditAgricoleStorageMock = mock(CreditAgricoleStorage.class);

        when(creditAgricoleStorageMock.getTokenFromStorage()).thenReturn(ACCESS_TOKEN);

        return creditAgricoleStorageMock;
    }
}
