package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.configuration.CrosskeyBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.transaction.TransactionExceptionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.rpc.CrosskeyTransactionsResponse;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.QsealcSignerImpl;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@RunWith(MockitoJUnitRunner.class)
public class CrosskeyBaseApiClientTest {

    @Mock private TinkHttpClient httpClientMock;
    @Mock private SessionStorage sessionStorage;
    @Mock private RequestBuilder requestBuilder;

    private CrosskeyBaseApiClient crosskeyApiClient;

    @Before
    public void setUp() {
        QsealcSigner qsealcSigner = mock(QsealcSignerImpl.class);
        AgentConfiguration<CrosskeyBaseConfiguration> agentConfiguration =
                mock(AgentConfiguration.class);
        when(agentConfiguration.getRedirectUrl()).thenReturn(CrossKeyTestUtils.REDIRECT_URL);
        CrosskeyBaseConfiguration crosskeyBaseConfiguration = mock(CrosskeyBaseConfiguration.class);
        when(crosskeyBaseConfiguration.getClientSecret())
                .thenReturn(CrossKeyTestUtils.CLIENT_SECRET);
        when(agentConfiguration.getProviderSpecificConfiguration())
                .thenReturn(crosskeyBaseConfiguration);

        crosskeyApiClient =
                new CrosskeyBaseApiClient(
                        httpClientMock,
                        sessionStorage,
                        CrossKeyTestUtils.getCrossKeyMarketConfiguration(),
                        agentConfiguration,
                        qsealcSigner,
                        CrossKeyTestUtils.CERTIFICATE_SERIAL_NUMBER);

        when(requestBuilder.header(anyString(), anyString())).thenReturn(requestBuilder);
    }

    @Test
    public void shouldFetchCreditCardTransactions() {
        // given
        setUpSuccessfulSessionStorage();
        CreditCardAccount creditCardAccount = mock(CreditCardAccount.class);
        when(creditCardAccount.getApiIdentifier()).thenReturn("apiIdentifier");
        when(httpClientMock.request(any(URL.class))).thenReturn(requestBuilder);
        when(requestBuilder.get(CrosskeyTransactionsResponse.class))
                .thenReturn(
                        CrossKeyTestUtils.loadResourceFileContent(
                                "checkingTransactions.json", CrosskeyTransactionsResponse.class));

        // when
        CrosskeyTransactionsResponse crosskeyTransactionsResponse =
                crosskeyApiClient.fetchCreditCardTransactions(
                        creditCardAccount,
                        CrossKeyTestUtils.PAGING_FROM,
                        CrossKeyTestUtils.PAGING_TO);
        List<Transaction> tinkTransactions =
                new ArrayList(crosskeyTransactionsResponse.getTinkTransactions());
        // then
        assertEquals(2, tinkTransactions.size());
    }

    @Test
    public void shouldFailAndFixDuringRetryOfFetchCreditCardTransactions() {
        setUpSuccessfulSessionStorage();
        CreditCardAccount creditCardAccount = mock(CreditCardAccount.class);
        when(creditCardAccount.getApiIdentifier()).thenReturn("apiIdentifier");
        when(httpClientMock.request(any(URL.class))).thenReturn(requestBuilder);

        HttpResponseException httpResponseException = mock(HttpResponseException.class);
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponseException.getResponse()).thenReturn(httpResponse);
        when(httpResponse.getStatus()).thenReturn(403);
        when(httpResponse.getBody(TransactionExceptionEntity.class))
                .thenReturn(
                        CrossKeyTestUtils.loadResourceFileContent(
                                "transactionFetchingException.json",
                                TransactionExceptionEntity.class));

        when(requestBuilder.get(CrosskeyTransactionsResponse.class))
                .thenThrow(httpResponseException)
                .thenReturn(
                        CrossKeyTestUtils.loadResourceFileContent(
                                "checkingTransactions.json", CrosskeyTransactionsResponse.class));

        // when
        CrosskeyTransactionsResponse crosskeyTransactionsResponse =
                crosskeyApiClient.fetchCreditCardTransactions(
                        creditCardAccount,
                        CrossKeyTestUtils.PAGING_FROM,
                        CrossKeyTestUtils.PAGING_TO);
        List<Transaction> tinkTransactions =
                new ArrayList(crosskeyTransactionsResponse.getTinkTransactions());
        // then
        assertEquals(false, crosskeyTransactionsResponse.canFetchMore().get());
        assertEquals(2, tinkTransactions.size());
    }

    private void setUpSuccessfulSessionStorage() {
        OAuth2Token oAuth2Token = OAuth2Token.create("Bearer", "123", null, 3600);
        when(sessionStorage.get(StorageKeys.TOKEN, OAuth2Token.class))
                .thenReturn(java.util.Optional.of(oAuth2Token));
        when(requestBuilder.addBearerToken(any())).thenReturn(requestBuilder);
    }
}
