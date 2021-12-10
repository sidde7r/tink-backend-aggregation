package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.apiClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.configuration.BelfiusConfiguration;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.header.AuthorizationHeader;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.amount.ExactCurrencyAmount;

@RunWith(MockitoJUnitRunner.class)
public class BelfiusApiClientTest {

    private static final OAuth2Token VALID_ACCESS_TOKEN =
            OAuth2Token.create("oauth2", "belfiusAccessToken", "belfiusRefreshToken", 98765);

    private static final String CLIENT_ID = "CLIENT_ID";
    private static final String NEXT_KEY = "NEXT_KEY";
    private static final String LOGICAL_ID = "LOGICAL_ID";

    @Mock private RequestBuilder requestBuilder;

    @Mock private AgentConfiguration<BelfiusConfiguration> configuration;

    @Mock private BelfiusConfiguration belfiusConfiguration;

    @Mock private RandomValueGenerator randomValueGenerator;

    @Mock private HttpResponse httpResponse;

    @Mock private TinkHttpClient client;

    private BelfiusApiClient apiClient;

    @Before
    public void setUp() {

        mockRequestBuilder();

        given(belfiusConfiguration.getClientId()).willReturn(CLIENT_ID);
        given(configuration.getProviderSpecificConfiguration()).willReturn(belfiusConfiguration);
        apiClient = new BelfiusApiClient(client, configuration, randomValueGenerator);
    }

    @Test
    public void shouldFetchTransactionsFrom90Days() {
        // given

        mockHttpResponse();

        // when
        FetchTransactionsResponse response =
                apiClient.fetchTransactionsFromLast90Days(VALID_ACCESS_TOKEN, NEXT_KEY, LOGICAL_ID);

        // then
        assertThatResponseIsCorrect(response);
    }

    @Test
    public void shouldFetchTransactionsFromDate() {
        // given
        final String scaToken = "DUMMY_SCA_TOKEN";
        final LocalDate dateFrom = LocalDate.of(1991, 9, 9);
        final String pageSize = "300";

        mockHttpResponse();

        // when
        FetchTransactionsResponse response =
                apiClient.fetchTransactionsFromDate(
                        VALID_ACCESS_TOKEN, NEXT_KEY, LOGICAL_ID, scaToken, dateFrom, pageSize);
        // then
        assertThatResponseIsCorrect(response);
    }

    private void mockRequestBuilder() {
        given(requestBuilder.acceptLanguage(ArgumentMatchers.<String>any()))
                .willReturn(requestBuilder);
        given(requestBuilder.header(any(), any())).willReturn(requestBuilder);
        given(requestBuilder.addBearerToken(any(AuthorizationHeader.class)))
                .willReturn(requestBuilder);
        given(requestBuilder.get(any())).willReturn(httpResponse);
        given(client.request(any(URL.class))).willReturn(requestBuilder);
    }

    private void mockHttpResponse() {
        given(httpResponse.getBody(any())).willReturn(BelfiusApiClientData.TRANSACTION_RESPONSE);
    }

    private void assertThatResponseIsCorrect(FetchTransactionsResponse response) {
        assertThat(response.getTinkTransactions()).isNotEmpty();
        assertThat(response.getTinkTransactions()).hasSize(3);
        Transaction transaction = response.getTinkTransactions().iterator().next();
        assertThat(transaction.getDescription()).isEqualTo("SEPA CREDIT TRANSFER from PSD2Company");
        assertThat(transaction.getAmount()).isEqualTo(ExactCurrencyAmount.of(12.25, "EUR"));
        assertThat(transaction.getDate().toString()).isEqualTo("Thu Jul 30 10:00:00 UTC 2020");
    }
}
