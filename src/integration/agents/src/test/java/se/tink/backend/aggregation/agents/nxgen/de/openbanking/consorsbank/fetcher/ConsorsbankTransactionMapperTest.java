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

    private final ConsorsbankTransactionMapper mapper = new ConsorsbankTransactionMapper();

    private FetchTransactionsResponse testTransactions;

    @Test
    public void shouldMapBothPendingAndBookedProperly() {
        // given
        testTransactions =
                TestDataReader.readFromFile(
                        TestDataReader.TRANSACTIONS_BOTH_KINDS, FetchTransactionsResponse.class);

        // when
        List<AggregationTransaction> transactions =
                mapper.toTinkTransactions(testTransactions.getTransactions());

        // then
        assertThat(transactions).hasSize(2);
        assertThat(transactions.stream().allMatch(x -> x instanceof Transaction)).isTrue();
        Transaction transaction = (Transaction) transactions.get(0);
        assertThat(transaction.isPending()).isFalse();
        assertThat(transaction.getDescription()).isEqualTo("CredName");
        assertThat(transaction.getAmount()).isEqualTo(ExactCurrencyAmount.of(-23.29, "EUR"));

        transaction = (Transaction) transactions.get(1);
        assertThat(transaction.isPending()).isTrue();
        assertThat(transaction.getDescription()).isEqualTo("CredName");
        assertThat(transaction.getAmount()).isEqualTo(ExactCurrencyAmount.of(-32.29, "EUR"));
    }

    @Test
    public void shouldMapDescriptionBothIncomeAndPurchaseProperly() {
        // given
        testTransactions =
                TestDataReader.readFromFile(
                        TestDataReader.TRANSACTIONS_INCOME_AND_PURCHASE,
                        FetchTransactionsResponse.class);

        // when
        List<AggregationTransaction> transactions =
                mapper.toTinkTransactions(testTransactions.getTransactions());

        // then

        // Purchase transaction
        Transaction transaction = (Transaction) transactions.get(0);
        assertThat(transaction.getDescription()).isEqualTo("CredName");

        // Income transaction
        transaction = (Transaction) transactions.get(1);
        assertThat(transaction.getDescription()).isEqualTo("DebtName");

        // Income transaction, no creditor name
        transaction = (Transaction) transactions.get(2);
        assertThat(transaction.getDescription()).isEqualTo("REMITTANCE_INFORMATION");

        // Purchase transaction, no debtor name
        transaction = (Transaction) transactions.get(3);
        assertThat(transaction.getDescription()).isEqualTo("REMITTANCE_INFORMATION");

        // Purchase transaction, PayPal, remittance information given
        transaction = (Transaction) transactions.get(4);
        assertThat(transaction.getDescription()).isEqualTo("REMITTANCE_INFORMATION");

        // Purchase transaction, Klarna, empty remittance information
        transaction = (Transaction) transactions.get(5);
        assertThat(transaction.getDescription()).isEqualTo("Klarna");

        assertThat(transactions.size()).isEqualTo(6);
    }
}
