package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.TestHelper.mockHttpClient;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Iterator;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1ApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.account.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class Sparebank1TransactionFetcherTest {
    private static final String RESOURCE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/banks/sparebank1/resources";

    @Test
    public void fetchAccountsShouldReturnTinkAccounts() {
        // given
        Sparebank1TransactionFetcher fetcher = prepareFetcher();
        TransactionalAccount account = mock(TransactionalAccount.class);
        when(account.getApiIdentifier()).thenReturn("dummyApiIdentifier");

        // when
        PaginatorResponse paginatorResponse =
                fetcher.getTransactionsFor(account, new Date(), new Date());

        // then
        assertThat(paginatorResponse).isNotNull();
        assertThat(paginatorResponse.getTinkTransactions()).hasSize(2);
        Iterator<? extends Transaction> iterator =
                paginatorResponse.getTinkTransactions().iterator();
        assertTransaction(
                iterator.next(),
                "description1",
                ExactCurrencyAmount.of(BigDecimal.valueOf(100), "NOK"),
                1602496800000L);
        assertTransaction(
                iterator.next(),
                "description2",
                ExactCurrencyAmount.of(BigDecimal.valueOf(-50.32), "NOK"),
                1612522800000L);
    }

    private void assertTransaction(
            Transaction transaction,
            String description,
            ExactCurrencyAmount amount,
            long timestamp) {
        assertThat(transaction.getAmount()).isEqualTo(amount);
        assertThat(transaction.getDescription()).isEqualTo(description);
        assertThat(transaction.getDate()).isEqualTo(new Date(timestamp));
    }

    private Sparebank1TransactionFetcher prepareFetcher() {
        HttpResponse response = mock(HttpResponse.class);
        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        when(requestBuilder.get(TransactionsResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(RESOURCE_PATH, "transaction_response.json").toFile(),
                                TransactionsResponse.class));
        TinkHttpClient client = mockHttpClient(requestBuilder, response);
        Sparebank1ApiClient apiClient = new Sparebank1ApiClient(client, "dummyBankId");
        return new Sparebank1TransactionFetcher(apiClient);
    }
}
