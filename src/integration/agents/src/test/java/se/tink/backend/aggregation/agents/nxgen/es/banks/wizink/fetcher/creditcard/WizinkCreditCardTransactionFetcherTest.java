package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.creditcard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.models.TransactionDateType;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.WizinkApiClient;
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
    private CreditCardAccount mockAccount;

    @Before
    public void setup() {
        wizinkApiClient = mock(WizinkApiClient.class);
        wizinkCreditCardTransactionFetcher =
                new WizinkCreditCardTransactionFetcher(wizinkApiClient);
        mockAccount = mock(CreditCardAccount.class);
    }

    @Test
    public void shouldFetchTransactionsFromLast90Days() {
        // given
        prepareData();

        // when
        List<AggregationTransaction> transactions =
                wizinkCreditCardTransactionFetcher.fetchTransactionsFor(mockAccount);

        // then
        assertThat(transactions).hasSize(2);
    }

    @Test
    public void shouldFetchAndMapTransactionsFromLast90Days() {
        // given
        prepareData();

        // when
        List<AggregationTransaction> transactions =
                wizinkCreditCardTransactionFetcher.fetchTransactionsFor(mockAccount);

        // then
        assertFirstTransactionData(transactions.get(0));
        assertSecondTransactionData(transactions.get(1));
    }

    private void prepareData() {
        when(mockAccount.getApiIdentifier()).thenReturn("dummyApiIdentifier");
        when(wizinkApiClient.fetchCreditCardTransactionsFrom90Days(any()))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(
                                                TEST_DATA_PATH,
                                                "find_movements_response_last_90_days.json")
                                        .toFile(),
                                FindMovementsResponse.class));
    }

    private void assertFirstTransactionData(AggregationTransaction transaction) {
        assertThat(transaction).isNotNull();
        assertThat(transaction.getDescription()).isEqualTo("MOVEMENT 1");
        assertThat(transaction.getDate().toString()).isEqualTo("Tue Mar 31 10:00:00 UTC 2020");
        assertThat(transaction.getAmount()).isEqualTo(ExactCurrencyAmount.of(-6, "EUR"));
        assertThat(transaction.getTransactionDates().getDates().size()).isEqualTo(2);
        assertThat(transaction.getTransactionDates().getDates().get(0).getType())
                .isEqualTo(TransactionDateType.VALUE_DATE);
        assertThat(transaction.getTransactionDates().getDates().get(1).getType())
                .isEqualTo(TransactionDateType.BOOKING_DATE);
    }

    private void assertSecondTransactionData(AggregationTransaction transaction) {
        assertThat(transaction).isNotNull();
        assertThat(transaction.getDescription()).isEqualTo("MOVEMENT 2");
        assertThat(transaction.getDate().toString()).isEqualTo("Tue Mar 24 11:00:00 UTC 2020");
        assertThat(transaction.getAmount()).isEqualTo(ExactCurrencyAmount.of(6.5, "EUR"));
        assertThat(transaction.getTransactionDates().getDates().size()).isEqualTo(2);
        assertThat(transaction.getTransactionDates().getDates().get(0).getType())
                .isEqualTo(TransactionDateType.VALUE_DATE);
        assertThat(transaction.getTransactionDates().getDates().get(1).getType())
                .isEqualTo(TransactionDateType.BOOKING_DATE);
    }
}
