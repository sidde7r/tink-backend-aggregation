package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.rpc.CreditCardTransactionResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class DnbCreditCardTransactionFetcherTest {
    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/openbanking/dnb/resources";
    private static DnbApiClient apiClient = mock(DnbApiClient.class);

    @Test
    public void shouldParseCreditCardTransactionsProperly() {
        // given
        DnbCreditCardTransactionFetcher fetcher = new DnbCreditCardTransactionFetcher(apiClient);
        CreditCardAccount account = mock(CreditCardAccount.class);
        when(apiClient.fetchCreditCardTransactions(account, null, null))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "cardTransactions.json").toFile(),
                                CreditCardTransactionResponse.class));

        // when
        PaginatorResponse transactions = fetcher.getTransactionsFor(account, null, null);
        Collection<? extends Transaction> tinkTransactions = transactions.getTinkTransactions();
        // then
        assertThat(tinkTransactions).hasSize(3);
        Iterator<? extends Transaction> iterator = tinkTransactions.iterator();
        assertTransaction(iterator.next(), 10.20, "2010-10-02", "ASDF Description 1");
        assertTransaction(iterator.next(), 510.20, "2000-10-02", "ZXCV Description 2");
        assertTransaction(iterator.next(), 150.20, "2020-10-02", "QWER Description 3");
    }

    private void assertTransaction(
            Transaction transaction,
            double expectedAmount,
            String expectedDate,
            String expectedDescription) {
        assertThat(transaction.getExactAmount().getDoubleValue()).isEqualTo(expectedAmount);
        assertThat(transaction.getDescription()).isEqualTo(expectedDescription);
        assertThat(ThreadSafeDateFormat.FORMATTER_DAILY.format(transaction.getDate()))
                .isEqualTo(expectedDate);
    }
}
