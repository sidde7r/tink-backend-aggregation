package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.WizinkApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.WizinkStorage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.account.rpc.ConsultTransactionResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class WizinkTransactionFetcherTest {
    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/wizink/resources/fetcher/account";
    private WizinkApiClient wizinkApiClient;
    private WizinkTransactionFetcher wizinkTransactionFetcher;
    private WizinkStorage wizinkStorage;
    private TransactionalAccount mockAccount;

    @Before
    public void setup() {
        wizinkApiClient = mock(WizinkApiClient.class);
        wizinkStorage = mock(WizinkStorage.class);
        wizinkTransactionFetcher = new WizinkTransactionFetcher(wizinkApiClient, wizinkStorage);
        mockAccount = mock(TransactionalAccount.class);
    }

    @Test
    public void shouldFetchTransactionFromLast90Days() {
        // given
        prepareDataWithFirstRefreshFlagOnFalse();

        // when
        List<AggregationTransaction> transactions =
                wizinkTransactionFetcher.fetchTransactionsFor(mockAccount);

        // then
        assertThat(transactions).hasSize(5);
    }

    @Test
    public void shouldFetchAllTransactions() {
        // given
        prepareDataWithFirstRefreshFlagOnTrue();

        // when
        List<AggregationTransaction> transactions =
                wizinkTransactionFetcher.fetchTransactionsFor(mockAccount);

        // then
        assertThat(transactions).hasSize(7);
    }

    @Test
    public void shouldFetchAndMapFirstTransaction() {
        // given
        prepareDataWithFirstRefreshFlagOnTrue();

        // when
        List<AggregationTransaction> transactions =
                wizinkTransactionFetcher.fetchTransactionsFor(mockAccount);

        // then
        assertFirstTransaction(transactions.get(0));
    }

    @Test
    public void shouldFetchAndMapLastTransaction() {
        // given
        prepareDataWithFirstRefreshFlagOnTrue();

        // when
        List<AggregationTransaction> transactions =
                wizinkTransactionFetcher.fetchTransactionsFor(mockAccount);

        // then
        assertLastTransaction(transactions.get(6));
    }

    private void prepareDataWithFirstRefreshFlagOnTrue() {
        when(wizinkStorage.getFirstFullRefreshFlag()).thenReturn(true);
        prepareData();
    }

    private void prepareDataWithFirstRefreshFlagOnFalse() {
        when(wizinkStorage.getFirstFullRefreshFlag()).thenReturn(false);
        prepareData();
    }

    private void prepareData() {
        when(mockAccount.getApiIdentifier()).thenReturn("dummy");
        when(wizinkApiClient.fetchTransactionsFrom90Days(any()))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(
                                                TEST_DATA_PATH,
                                                "consult_transaction_response_last_90_days.json")
                                        .toFile(),
                                ConsultTransactionResponse.class));
        when(wizinkApiClient.fetchSessionIdForOlderAccountTransactions(any()))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(
                                                TEST_DATA_PATH,
                                                "consult_transaction_response_sessionId.json")
                                        .toFile(),
                                ConsultTransactionResponse.class));

        when(wizinkApiClient.fetchTransactionsOlderThan90Days(any(), any()))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(
                                                TEST_DATA_PATH,
                                                "consult_transaction_response_more_than_90_days.json")
                                        .toFile(),
                                ConsultTransactionResponse.class));
    }

    private void assertFirstTransaction(AggregationTransaction transaction) {
        assertThat(transaction.getDescription()).isEqualTo("INTERESES");
        assertThat(transaction.getDate().toString()).isEqualTo("Thu Apr 01 10:00:00 UTC 2021");
        assertThat(transaction.getExactAmount()).isEqualTo(ExactCurrencyAmount.inEUR(15.30));
    }

    private void assertLastTransaction(AggregationTransaction transaction) {
        assertThat(transaction.getDescription()).isEqualTo("INTERESES CUENTA AHORRO");
        assertThat(transaction.getDate().toString()).isEqualTo("Mon Feb 03 11:00:00 UTC 2020");
        assertThat(transaction.getExactAmount()).isEqualTo(ExactCurrencyAmount.inEUR(0));
    }
}
