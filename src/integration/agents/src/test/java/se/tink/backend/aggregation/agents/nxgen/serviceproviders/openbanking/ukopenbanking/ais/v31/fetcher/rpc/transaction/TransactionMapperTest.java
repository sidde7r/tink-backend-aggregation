package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.rpc.transaction;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.backend.aggregation.agents.models.TransactionDateType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.entities.transaction.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures.TransactionEntityFixtures;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class TransactionMapperTest {

    private final TransactionMapper transactionMapper = TransactionMapper.getDefault();

    @Test
    public void shouldParseTransactionWithMutabilityField() {
        // given
        TransactionEntity transactionEntity =
                TransactionEntityFixtures.getTransactionEntityWithMutabilityField();

        // when
        Transaction transaction = transactionMapper.toTinkTransaction(transactionEntity);

        // then
        assertThat(transaction.getMutable()).isTrue();
        assertThat(transaction.getTransactionDates().getDates().size()).isEqualTo(1);
        assertThat(transaction.getTransactionDates().getDates().get(0).getType())
                .isEqualTo(TransactionDateType.BOOKING_DATE);
    }

    @Test
    public void shouldParseTransactionWithoutMutabilityField() {
        // given
        TransactionEntity transactionEntity =
                TransactionEntityFixtures.getTransactionEntityWithoutMutabilityField();

        // when
        Transaction transaction = transactionMapper.toTinkTransaction(transactionEntity);

        // then
        assertThat(transaction.getMutable()).isFalse();
        assertThat(transaction.getTransactionDates().getDates().size()).isEqualTo(1);
        assertThat(transaction.getTransactionDates().getDates().get(0).getType())
                .isEqualTo(TransactionDateType.BOOKING_DATE);
    }

    @Test
    public void shouldParseTransactionWithIncompletedTransaction() {
        // given
        TransactionEntity transactionEntity =
                TransactionEntityFixtures.getTransactionEntityWithIncompletedTransaction();

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
                TransactionEntityFixtures.getTransactionEntityWithFullyCompletedTransaction();

        // when
        Transaction transaction = transactionMapper.toTinkTransaction(transactionEntity);

        // then
        assertThat(transaction.getMutable()).isFalse();
        assertThat(transaction.getTransactionDates().getDates().size()).isEqualTo(1);
        assertThat(transaction.getTransactionDates().getDates().get(0).getType())
                .isEqualTo(TransactionDateType.BOOKING_DATE);
    }
}
