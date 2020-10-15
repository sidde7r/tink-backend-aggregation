package se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.NorwegianConstants;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.client.NorwegianApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher.account.data.NorwegianFetcherTestData;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class NorwegianTransactionFetcherTest {

    NorwegianTransactionFetcher fetcher;
    TransactionalAccount someAccount;

    @Before
    public void init() {
        PersistentStorage persistentStorage = mock(PersistentStorage.class);
        NorwegianApiClient client = mock(NorwegianApiClient.class);
        when(persistentStorage.get(NorwegianConstants.StorageKeys.CONSENT_CREATION_DATE))
                .thenReturn(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        when(client.getTransactions(any(), any(), any(), anyInt()))
                .thenReturn(NorwegianFetcherTestData.getTransactionResponse());
        fetcher =
                new NorwegianTransactionFetcher(
                        client, persistentStorage, mock(SessionStorage.class));
        someAccount = mock(TransactionalAccount.class);
    }

    @Test
    public void shouldReturnAndMapCorrectTransactions() {
        // when
        List<Transaction> transactions =
                new ArrayList<>(
                        fetcher.getTransactionsFor(someAccount, new Date(), new Date())
                                .getTinkTransactions());
        // then
        assertThat(transactions).hasSize(5);

        transactions.sort(Comparator.comparing(AggregationTransaction::getDescription));

        assertThat(transactions.get(0).getDescription())
                .isEqualTo(NorwegianFetcherTestData.TRANSACTION_0_DESCRIPTION);
        assertThat(transactions.get(0).getExactAmount().getDoubleValue())
                .isEqualTo(Double.parseDouble(NorwegianFetcherTestData.TRANSACTION_0_AMOUNT));
        assertThat(transactions.get(0).isPending()).isFalse();

        assertThat(transactions.get(1).getDescription())
                .isEqualTo(NorwegianFetcherTestData.TRANSACTION_1_DESCRIPTION);
        assertThat(transactions.get(1).getExactAmount().getDoubleValue())
                .isEqualTo(Double.parseDouble(NorwegianFetcherTestData.TRANSACTION_1_AMOUNT));
        assertThat(transactions.get(1).isPending()).isFalse();

        assertThat(transactions.get(2).getDescription())
                .isEqualTo(NorwegianFetcherTestData.TRANSACTION_2_DESCRIPTION);
        assertThat(transactions.get(2).getExactAmount().getDoubleValue())
                .isEqualTo(Double.parseDouble(NorwegianFetcherTestData.TRANSACTION_2_AMOUNT));
        assertThat(transactions.get(2).isPending()).isFalse();

        assertThat(transactions.get(3).getDescription())
                .isEqualTo(NorwegianFetcherTestData.TRANSACTION_3_DESCRIPTION);
        assertThat(transactions.get(3).getExactAmount().getDoubleValue())
                .isEqualTo(Double.parseDouble(NorwegianFetcherTestData.TRANSACTION_3_AMOUNT));
        assertThat(transactions.get(3).isPending()).isTrue();

        assertThat(transactions.get(4).getDescription())
                .isEqualTo(NorwegianFetcherTestData.TRANSACTION_4_DESCRIPTION);
        assertThat(transactions.get(4).getExactAmount().getDoubleValue())
                .isEqualTo(Double.parseDouble(NorwegianFetcherTestData.TRANSACTION_4_AMOUNT));
        assertThat(transactions.get(4).isPending()).isTrue();
    }
}
