package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeTestHelper.createCbiGlobeApiClient;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeTestHelper.mockHttpClient;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Collection;
import javax.ws.rs.core.MultivaluedMap;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CbiGlobeApiClientTest {

    private final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/cbiglobe/resources";

    @Test
    public void empty_transactions_are_not_converted_to_tink_transactions() {
        // given
        HttpResponse httpResponse = mock(HttpResponse.class);
        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        TinkHttpClient tinkHttpClient = mockHttpClient(httpResponse, requestBuilder);
        mockEmptyTransactionsResponses(httpResponse);

        CbiGlobeApiClient cbiGlobeApiClient = createCbiGlobeApiClient(tinkHttpClient);

        // when
        GetTransactionsResponse getTransactionsResponse =
                cbiGlobeApiClient.getTransactions(
                        "apiIdentifier", LocalDate.now(), LocalDate.now(), "bookingType", 1);
        Collection<? extends Transaction> transactions =
                getTransactionsResponse.getTinkTransactions();

        // then
        assertThat(transactions).isEmpty();
    }

    @Test
    public void null_account_are_not_converted_to_account() {
        // given
        HttpResponse httpResponse = mock(HttpResponse.class);
        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        TinkHttpClient tinkHttpClient = mockHttpClient(httpResponse, requestBuilder);
        mockNullAccountResponses(requestBuilder);

        CbiGlobeApiClient cbiGlobeApiClient = createCbiGlobeApiClient(tinkHttpClient);

        // when
        GetAccountsResponse accountsResponse = cbiGlobeApiClient.getAccounts();

        // then
        assertThat(accountsResponse.getAccounts()).isEmpty();
    }

    private void mockEmptyTransactionsResponses(HttpResponse httpResponse) {
        MultivaluedMap<String, String> multivaluedMap = new MultivaluedMapImpl();
        multivaluedMap.putSingle(QueryKeys.TOTAL_PAGES, "1");
        when(httpResponse.getHeaders()).thenReturn(multivaluedMap);

        when(httpResponse.getBody(GetTransactionsResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "transactions_response.json").toFile(),
                                GetTransactionsResponse.class));
    }

    private void mockNullAccountResponses(RequestBuilder requestBuilder) {
        when(requestBuilder.get(GetAccountsResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "accounts_response.json").toFile(),
                                GetAccountsResponse.class));
    }
}
