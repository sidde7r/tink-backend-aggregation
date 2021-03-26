package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenStorage;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SparkassenTransactionsFetcherTest {

    private SparkassenApiClient apiClient;
    private SparkassenStorage persistentStorage;
    private SparkassenTransactionsFetcher transactionsFetcher;
    private TransactionalAccount account;

    private static final String TEST_CONSENT_ID = "CONSENT_ID";
    private static final String ACCOUNT_ID = "accountIdentifier12345";

    @Before
    public void setup() {
        apiClient = mock(SparkassenApiClient.class);
        persistentStorage = new SparkassenStorage(new PersistentStorage());
        persistentStorage.saveConsentId(TEST_CONSENT_ID);
        transactionsFetcher = new SparkassenTransactionsFetcher(apiClient, persistentStorage);
        account = mock(TransactionalAccount.class);
        when(account.getApiIdentifier()).thenReturn(ACCOUNT_ID);
    }

    @Test
    public void shouldReturnEmptyListOfTransactionsWhenNoTransactionsReturnedFromBank() {
        // given
        when(apiClient.fetchTransactions(eq(TEST_CONSENT_ID), eq(ACCOUNT_ID), any(LocalDate.class)))
                .thenReturn(FetcherTestData.getTransactionsResponseWithNoTransactions());

        // when
        List<AggregationTransaction> transactions =
                transactionsFetcher.fetchTransactionsFor(account);

        // then
        assertThat(transactions).hasSize(0);
    }

    @Test
    public void shouldReturnFourTransactions() {
        // given
        when(apiClient.fetchTransactions(eq(TEST_CONSENT_ID), eq(ACCOUNT_ID), any(LocalDate.class)))
                .thenReturn(FetcherTestData.getTransactionsResponse());

        // when
        List<AggregationTransaction> transactions =
                transactionsFetcher.fetchTransactionsFor(account);

        // then
        assertThat(transactions).hasSize(4);
    }

    @Test
    public void shouldThrowIllegalStateExceptionWhenFailedToParseResponseFromBank() {
        // given
        when(apiClient.fetchTransactions(eq(TEST_CONSENT_ID), eq(ACCOUNT_ID), any(LocalDate.class)))
                .thenReturn("");

        // when
        Throwable throwable =
                catchThrowable(() -> transactionsFetcher.fetchTransactionsFor(account));

        // then
        assertThat(throwable).isInstanceOf(IllegalStateException.class);
    }
}
