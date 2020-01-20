package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.apiclient;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class CreditAgricoleBaseApiClientTest {

    private CreditAgricoleBaseApiClient apiClient;
    private PersistentStorage persistentStorage;
    private TinkHttpClient httpClient;
    private RequestFactory requestFactory;

    @Before
    public void setUp() throws Exception {
        persistentStorage = mock(PersistentStorage.class);
        httpClient = mock(TinkHttpClient.class);
        requestFactory = mock(RequestFactory.class);

        apiClient = new CreditAgricoleBaseApiClient(httpClient, persistentStorage, requestFactory);
    }

    @Test
    public void ifFetchTransactionsReturns204_EmptyResponseIsReturned() {
        // given
        HttpResponse noContentResponse = mock(HttpResponse.class);
        when(noContentResponse.getStatus()).thenReturn(204);

        // when
        when(requestFactory.constructFetchTransactionRequest(
                        anyString(), any(), any(), any(), any()))
                .thenReturn(mock(HttpRequest.class));
        when(httpClient.request(any(), any(HttpRequest.class))).thenReturn(noContentResponse);

        GetTransactionsResponse transactions =
                apiClient.getTransactions("123ID", new Date(), new Date());

        // then
        Assertions.assertThat(transactions.getTinkTransactions()).isEmpty();
    }

    @Test
    public void ifFetchTransactionsReturns200_bodyIsExtracted() {
        // given
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatus()).thenReturn(200);
        when(response.getBody(GetTransactionsResponse.class))
                .thenReturn(TransactionsFixtures.transactionsResponse);

        // when
        when(requestFactory.constructFetchTransactionRequest(
                        anyString(), any(), any(), any(), any()))
                .thenReturn(mock(HttpRequest.class));
        when(httpClient.request(any(), any(HttpRequest.class))).thenReturn(response);

        GetTransactionsResponse transactions =
                apiClient.getTransactions("123ID", new Date(), new Date());

        // then
        Assertions.assertThat(transactions).isEqualTo(TransactionsFixtures.transactionsResponse);
        Assertions.assertThat(transactions.getTinkTransactions()).isNotEmpty();
    }
}
