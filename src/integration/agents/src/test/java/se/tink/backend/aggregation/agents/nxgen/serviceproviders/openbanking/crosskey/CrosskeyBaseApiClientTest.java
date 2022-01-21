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
import se.tink.agent.sdk.operation.User;
import se.tink.agent.sdk.utils.signer.qsealc.QsealcSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.configuration.CrosskeyBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.rpc.CrosskeyTransactionsResponse;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.configuration.EIdasTinkCert;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@RunWith(MockitoJUnitRunner.class)
public class CrosskeyBaseApiClientTest {

    @Mock private TinkHttpClient httpClientMock;
    @Mock private SessionStorage sessionStorage;
    @Mock private RequestBuilder requestBuilder;
    @Mock private User user;

    private CrosskeyBaseApiClient crosskeyApiClient;

    @Before
    public void setUp() {
        QsealcSigner qsealcSigner = mock(QsealcSigner.class);
        AgentConfiguration<CrosskeyBaseConfiguration> agentConfiguration =
                mock(AgentConfiguration.class);
        when(agentConfiguration.getRedirectUrl()).thenReturn(CrossKeyTestUtils.REDIRECT_URL);
        CrosskeyBaseConfiguration crosskeyBaseConfiguration = mock(CrosskeyBaseConfiguration.class);
        when(crosskeyBaseConfiguration.getClientSecret())
                .thenReturn(CrossKeyTestUtils.CLIENT_SECRET);
        when(agentConfiguration.getProviderSpecificConfiguration())
                .thenReturn(crosskeyBaseConfiguration);
        when(agentConfiguration.getQsealc()).thenReturn(EIdasTinkCert.QSEALC);
        when(user.getIpAddress()).thenReturn("userIp");

        crosskeyApiClient =
                new CrosskeyBaseApiClient(
                        httpClientMock,
                        sessionStorage,
                        CrossKeyTestUtils.getCrossKeyMarketConfiguration(),
                        user,
                        agentConfiguration,
                        qsealcSigner,
                        "FI");

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
        when(user.isPresent()).thenReturn(false);

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
    public void shouldNotFetchMoreIfUserNotPresent() {
        setUpSuccessfulSessionStorage();
        CreditCardAccount creditCardAccount = mock(CreditCardAccount.class);
        when(creditCardAccount.getApiIdentifier()).thenReturn("apiIdentifier");
        when(httpClientMock.request(any(URL.class))).thenReturn(requestBuilder);
        when(requestBuilder.get(CrosskeyTransactionsResponse.class))
                .thenReturn(
                        CrossKeyTestUtils.loadResourceFileContent(
                                "checkingTransactions.json", CrosskeyTransactionsResponse.class));
        when(user.isPresent()).thenReturn(false);

        CrosskeyTransactionsResponse crosskeyTransactionsResponse =
                crosskeyApiClient.fetchCreditCardTransactions(
                        creditCardAccount,
                        CrossKeyTestUtils.PAGING_FROM,
                        CrossKeyTestUtils.PAGING_TO);
        List<Transaction> tinkTransactions =
                new ArrayList(crosskeyTransactionsResponse.getTinkTransactions());

        assertEquals(2, tinkTransactions.size());
        assertEquals(false, crosskeyTransactionsResponse.canFetchMore().get());
    }

    @Test
    public void shouldFetchMoreIfUserIsPresent() {
        setUpSuccessfulSessionStorage();
        CreditCardAccount creditCardAccount = mock(CreditCardAccount.class);
        when(creditCardAccount.getApiIdentifier()).thenReturn("apiIdentifier");
        when(httpClientMock.request(any(URL.class))).thenReturn(requestBuilder);
        when(requestBuilder.get(CrosskeyTransactionsResponse.class))
                .thenReturn(
                        CrossKeyTestUtils.loadResourceFileContent(
                                "checkingTransactions.json", CrosskeyTransactionsResponse.class));
        when(user.isPresent()).thenReturn(true);

        CrosskeyTransactionsResponse crosskeyTransactionsResponse =
                crosskeyApiClient.fetchCreditCardTransactions(
                        creditCardAccount,
                        CrossKeyTestUtils.PAGING_FROM,
                        CrossKeyTestUtils.PAGING_TO);
        List<Transaction> tinkTransactions =
                new ArrayList(crosskeyTransactionsResponse.getTinkTransactions());

        assertEquals(2, tinkTransactions.size());
        assertEquals(true, crosskeyTransactionsResponse.canFetchMore().get());
    }

    private void setUpSuccessfulSessionStorage() {
        OAuth2Token oAuth2Token = OAuth2Token.create("Bearer", "123", null, 3600);
        when(sessionStorage.get(StorageKeys.TOKEN, OAuth2Token.class))
                .thenReturn(java.util.Optional.of(oAuth2Token));
        when(requestBuilder.addBearerToken(any())).thenReturn(requestBuilder);
    }
}
