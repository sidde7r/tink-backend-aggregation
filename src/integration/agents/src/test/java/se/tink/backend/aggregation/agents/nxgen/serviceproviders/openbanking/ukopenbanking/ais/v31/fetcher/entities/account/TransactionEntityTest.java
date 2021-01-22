package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.entities.account;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.backend.aggregation.agents.models.TransactionDateType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.entities.transaction.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures.TransactionEntityFixtures;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class TransactionEntityTest {

    @Test
    public void shouldParseTransactionWithMutabilityField() {
        // given
        TransactionEntity transactionEntity =
                TransactionEntityFixtures.getTransactionEntityWithMutabilityField();

        // when
        Transaction transaction = transactionEntity.toTinkTransaction();

        // then
        assertThat(transaction.getMutable()).isTrue();
        assertThat(transaction.getTransactionDates().size()).isEqualTo(1);
        assertThat(transaction.getTransactionDates().get(0).getType())
                .isEqualTo(TransactionDateType.BOOKING_DATE);
    }

    @Test
    public void shouldParseTransactionWithoutMutabilityField() {
        // given
        TransactionEntity transactionEntity =
                TransactionEntityFixtures.getTransactionEntityWithoutMutabilityField();

        // when
        Transaction transaction = transactionEntity.toTinkTransaction();

        // then
        assertThat(transaction.getMutable()).isFalse();
        assertThat(transaction.getTransactionDates().size()).isEqualTo(1);
        assertThat(transaction.getTransactionDates().get(0).getType())
                .isEqualTo(TransactionDateType.BOOKING_DATE);
    }

    @Test
    public void shouldParseTransactionWithIncompletedTransaction() {
        // given
        TransactionEntity transactionEntity =
                TransactionEntityFixtures.getTransactionEntityWithIncompletedTransaction();

        // when
        Transaction transaction = transactionEntity.toTinkTransaction();

        // then
        assertThat(transaction.getMutable()).isTrue();
        assertThat(transaction.getTransactionDates().size()).isEqualTo(1);
        assertThat(transaction.getTransactionDates().get(0).getType())
                .isEqualTo(TransactionDateType.BOOKING_DATE);
    }

    @Test
    public void shouldParseTransactionWithFullyCompletedTransaction() {
        // given
        TransactionEntity transactionEntity =
                TransactionEntityFixtures.getTransactionEntityWithFullyCompletedTransaction();

        // when
        Transaction transaction = transactionEntity.toTinkTransaction();

        // then
        assertThat(transaction.getMutable()).isFalse();
        assertThat(transaction.getTransactionDates().size()).isEqualTo(1);
        assertThat(transaction.getTransactionDates().get(0).getType())
                .isEqualTo(TransactionDateType.BOOKING_DATE);
    }
}
