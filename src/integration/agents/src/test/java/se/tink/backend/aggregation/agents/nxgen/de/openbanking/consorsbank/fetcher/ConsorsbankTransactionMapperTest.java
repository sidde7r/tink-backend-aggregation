package se.tink.backend.aggregation.agents.nxgen.de.openbanking.consorsbank.fetcher;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.consorsbank.TestDataReader;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class ConsorsbankTransactionMapperTest {

    private ConsorsbankTransactionMapper mapper = new ConsorsbankTransactionMapper();

    private FetchTransactionsResponse testTransactions =
            TestDataReader.readFromFile(
                    TestDataReader.TRANSACTIONS_BOTH_KINDS, FetchTransactionsResponse.class);

    @Test
    public void shouldMapBothPendingAndBookedProperly() {
        // given

        // when
        List<AggregationTransaction> transactions =
                mapper.toTinkTransactions(testTransactions.getTransactions());

        // then
        assertThat(transactions).hasSize(2);
        assertThat(transactions.stream().allMatch(x -> x instanceof Transaction)).isTrue();
        Transaction transaction = (Transaction) transactions.get(0);
        assertThat(transaction.isPending()).isFalse();
        assertThat(transaction.getDescription())
                .isEqualTo(
                        "Transfer from account number: DE1234 Transfer to: CredName, account number: DE4321. Additional transaction description: BOOKED_TRANSACTION");
        assertThat(transaction.getAmount()).isEqualTo(ExactCurrencyAmount.of(-23.29, "EUR"));

        transaction = (Transaction) transactions.get(1);
        assertThat(transaction.isPending()).isTrue();
        assertThat(transaction.getDescription())
                .isEqualTo(
                        "Transfer from account number: DE1234 Transfer to: CredName, account number: DE4321. Additional transaction description: PENDING_TRANSACTION");
        assertThat(transaction.getAmount()).isEqualTo(ExactCurrencyAmount.of(-32.29, "EUR"));
    }
}
