package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.creditcard;

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
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.creditcard.rpc.FindMovementsResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class WizinkCreditCardTransactionFetcherTest {
    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/wizink/resources/fetcher/creditcard";

    private WizinkApiClient wizinkApiClient;
    private WizinkCreditCardTransactionFetcher wizinkCreditCardTransactionFetcher;
    private WizinkStorage wizinkStorage;
    private CreditCardAccount mockAccount;

    @Before
    public void setup() {
        wizinkApiClient = mock(WizinkApiClient.class);
        wizinkStorage = mock(WizinkStorage.class);
        wizinkCreditCardTransactionFetcher =
                new WizinkCreditCardTransactionFetcher(wizinkApiClient, wizinkStorage);
        mockAccount = mock(CreditCardAccount.class);
    }

    @Test
    public void shouldFetchTransactionFromLast90Days() {
        // given
        prepareData(false);

        // when
        List<AggregationTransaction> transactions =
                wizinkCreditCardTransactionFetcher.fetchTransactionsFor(mockAccount);

        // then
        assertThat(transactions).hasSize(2);
    }

    @Test
    public void shouldFetchAllTransactions() {
        // given
        prepareData(true);

        // when
        List<AggregationTransaction> transactions =
                wizinkCreditCardTransactionFetcher.fetchTransactionsFor(mockAccount);

        // then
        assertThat(transactions).hasSize(5);
    }

    @Test
    public void shouldFetchAndMapFirstAndLastTransaction() {
        // given
        prepareData(true);

        // when
        List<AggregationTransaction> transactions =
                wizinkCreditCardTransactionFetcher.fetchTransactionsFor(mockAccount);

        // then
        assertFirstTransactionData(transactions.get(0));
        assertFifthTransactionData(transactions.get(4));
    }

    private void prepareData(boolean firstRefresh) {
        when(mockAccount.getApiIdentifier()).thenReturn("dummyApiIdentifier");
        when(wizinkStorage.getFirstFullRefreshFlag()).thenReturn(firstRefresh);

        when(wizinkApiClient.fetchCreditCardTransactionsFrom90Days(any()))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(
                                                TEST_DATA_PATH,
                                                "find_movements_response_last_90_days.json")
                                        .toFile(),
                                FindMovementsResponse.class));
        when(wizinkApiClient.prepareOtpRequestToUserMobilePhone(any()))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "find_movements_response_sessionId.json")
                                        .toFile(),
                                FindMovementsResponse.class));

        when(wizinkApiClient.fetchCreditCardTransactionsOlderThan90Days(any(), any()))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(
                                                TEST_DATA_PATH,
                                                "find_movements_response_more_than_90_days.json")
                                        .toFile(),
                                FindMovementsResponse.class));
    }

    private void assertFirstTransactionData(AggregationTransaction transaction) {
        assertThat(transaction).isNotNull();
        assertThat(transaction.getDescription()).isEqualTo("MOVEMENT 1");
        assertThat(transaction.getDate().toString()).isEqualTo("Tue Mar 31 10:00:00 UTC 2020");
        assertThat(transaction.getAmount()).isEqualTo(ExactCurrencyAmount.of(-6, "EUR"));
    }

    private void assertFifthTransactionData(AggregationTransaction transaction) {
        assertThat(transaction).isNotNull();
        assertThat(transaction.getDescription()).isEqualTo("MOVEMENT 5");
        assertThat(transaction.getDate().toString()).isEqualTo("Fri Jan 24 11:00:00 UTC 2020");
        assertThat(transaction.getAmount()).isEqualTo(ExactCurrencyAmount.of(18, "EUR"));
    }
}
