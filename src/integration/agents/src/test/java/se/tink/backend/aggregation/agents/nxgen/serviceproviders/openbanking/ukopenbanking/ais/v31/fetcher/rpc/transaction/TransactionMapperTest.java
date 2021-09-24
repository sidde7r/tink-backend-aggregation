package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.rpc.transaction;

import static org.assertj.core.api.Assertions.assertThat;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.models.TransactionDateType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.entities.transaction.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures.TransactionEntityFixtures;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@RunWith(JUnitParamsRunner.class)
public class TransactionMapperTest {

    private final TransactionMapper transactionMapper = TransactionMapper.getDefault();

    @Test
    public void shouldNotSpecifyTransactionMutability() {
        // given
        TransactionEntity transactionEntity =
                TransactionEntityFixtures.getBookedTransactionWithUnspecifiedMutability();

        // when
        Transaction transaction = transactionMapper.toTinkTransaction(transactionEntity);

        // then
        assertThat(transaction.getMutable()).isNull();
    }

    @Test
    @Parameters(method = "mutableTransactions")
    public void shouldFlagTransactionAsMutable(TransactionEntity transactionEntity) {
        // when
        Transaction transaction = transactionMapper.toTinkTransaction(transactionEntity);

        // then
        assertThat(transaction.getMutable()).isTrue();
        assertThat(transaction.getTransactionDates().getDates().size()).isEqualTo(1);
        assertThat(transaction.getTransactionDates().getDates().get(0).getType())
                .isEqualTo(TransactionDateType.BOOKING_DATE);
    }

    @Test
    public void shouldParseTransactionWithFullyCompletedTransaction() {
        // given
        TransactionEntity transactionEntity =
                TransactionEntityFixtures.getImmutableBookedTransaction();

        // when
        Transaction transaction = transactionMapper.toTinkTransaction(transactionEntity);

        // then
        assertThat(transaction.getMutable()).isFalse();
        assertThat(transaction.getTransactionDates().getDates().size()).isEqualTo(1);
        assertThat(transaction.getTransactionDates().getDates().get(0).getType())
                .isEqualTo(TransactionDateType.BOOKING_DATE);
    }

    @SuppressWarnings("unused")
    private Object[] mutableTransactions() {
        return new Object[] {
            new Object[] {TransactionEntityFixtures.getMutableBookedTransaction()},
            new Object[] {TransactionEntityFixtures.getMutablePendingTransaction()}
        };
    }
}
