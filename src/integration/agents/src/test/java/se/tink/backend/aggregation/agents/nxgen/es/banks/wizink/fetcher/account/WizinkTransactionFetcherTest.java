package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.account;

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
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.account.rpc.ConsultTransactionResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class WizinkTransactionFetcherTest {
    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/wizink/resources/fetcher/account";
    private WizinkApiClient wizinkApiClient;
    private WizinkTransactionFetcher wizinkTransactionFetcher;
    private TransactionalAccount mockAccount;

    @Before
    public void setup() {
        wizinkApiClient = mock(WizinkApiClient.class);
        wizinkTransactionFetcher = new WizinkTransactionFetcher(wizinkApiClient);
        mockAccount = mock(TransactionalAccount.class);
    }

    @Test
    public void shouldFetchTransactionsFromLast90Days() {
        // given
        prepareData();

        // when
        List<AggregationTransaction> transactions =
                wizinkTransactionFetcher.fetchTransactionsFor(mockAccount);

        // then
        assertThat(transactions).hasSize(5);
    }

    @Test
    public void shouldFetchAndMapTransactionsFromLast90Days() {
        // given
        prepareData();

        // when
        List<AggregationTransaction> transactions =
                wizinkTransactionFetcher.fetchTransactionsFor(mockAccount);

        // then
        assertFirstTransactionData(transactions.get(0));
        assertSecondTransactionData(transactions.get(1));
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
    }

    private void assertFirstTransactionData(AggregationTransaction transaction) {
        assertThat(transaction.getDescription()).isEqualTo("INTERESES");
        assertThat(transaction.getDate().toString()).isEqualTo("Thu Apr 01 10:00:00 UTC 2021");
        assertThat(transaction.getExactAmount()).isEqualTo(ExactCurrencyAmount.inEUR(15.30));
        assertThat(transaction.getTransactionReference()).isEqualTo("915989955688000");
        assertThat(transaction.getProviderMarket()).isEqualTo(MarketCode.ES.toString());
        assertThat(transaction.getTransactionDates().getDates().size()).isEqualTo(2);
        assertThat(transaction.getTransactionDates().getDates().get(0).getType())
                .isEqualTo(TransactionDateType.VALUE_DATE);
        assertThat(transaction.getTransactionDates().getDates().get(1).getType())
                .isEqualTo(TransactionDateType.BOOKING_DATE);
    }

    private void assertSecondTransactionData(AggregationTransaction transaction) {
        assertThat(transaction.getDescription()).isEqualTo("INTERESES CUENTA");
        assertThat(transaction.getDate().toString()).isEqualTo("Sat Jan 16 11:00:00 UTC 2021");
        assertThat(transaction.getExactAmount()).isEqualTo(ExactCurrencyAmount.inEUR(0));
        assertThat(transaction.getTransactionReference()).isEqualTo("915964852620000");
        assertThat(transaction.getProviderMarket()).isEqualTo(MarketCode.ES.toString());
        assertThat(transaction.getTransactionDates().getDates().size()).isEqualTo(2);
        assertThat(transaction.getTransactionDates().getDates().get(0).getType())
                .isEqualTo(TransactionDateType.VALUE_DATE);
        assertThat(transaction.getTransactionDates().getDates().get(1).getType())
                .isEqualTo(TransactionDateType.BOOKING_DATE);
    }
}
