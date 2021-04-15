package se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.fetcher;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.TestDataReader;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.fetcher.data.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.fetcher.data.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class TransactionMapperTest {

    @Test
    public void shouldMapTransactionProperly() {
        // given
        TransactionMapper transactionMapper = new TransactionMapper();
        TransactionEntity transactionEntity =
                TestDataReader.readFromFile(
                                TestDataReader.TWO_TRANSACTIONS, TransactionsResponse.class)
                        .getTransactions()
                        .get(0);

        // when
        Optional<Transaction> maybeTransaction =
                transactionMapper.toTinkTransaction(transactionEntity);

        // then
        assertThat(maybeTransaction.isPresent()).isTrue();
        Transaction transaction = maybeTransaction.get();
        assertThat(transaction.isPending()).isFalse();
        assertThat(transaction.getAmount()).isEqualTo(ExactCurrencyAmount.of(1234, "EUR"));
        assertThat(transaction.getDescription()).isEqualTo("Super transaction information 001");
        assertThat(transaction.getDate()).hasYear(2020);
        assertThat(transaction.getDate()).hasMonth(10);
        assertThat(transaction.getDate()).hasDayOfMonth(12);
    }
}
