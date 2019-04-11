package se.tink.backend.aggregation.agents.abnamro.client.model.creditcards;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.backend.aggregation.agents.abnamro.client.model.AmountEntity;

public class TransactionContainerEntityTest {

    @Test
    public void testTransactionIsInEuro() throws Exception {
        TransactionContainerEntity entity = new TransactionContainerEntity();

        TransactionEntity transactionEntity = new TransactionEntity();

        AmountEntity amountEntity = new AmountEntity();

        amountEntity.setAmount(100D);
        amountEntity.setCurrencyCode("EUR");

        transactionEntity.setBillingAmount(amountEntity);

        entity.setCreditCardTransaction(transactionEntity);

        assertThat(entity.isInEUR()).isTrue();
    }

    @Test
    public void testTransactionIsNotInEuro() throws Exception {
        TransactionContainerEntity entity = new TransactionContainerEntity();

        TransactionEntity transactionEntity = new TransactionEntity();

        AmountEntity amountEntity = new AmountEntity();

        amountEntity.setAmount(100D);
        amountEntity.setCurrencyCode("SEK");

        transactionEntity.setBillingAmount(amountEntity);

        entity.setCreditCardTransaction(transactionEntity);

        assertThat(entity.isInEUR()).isFalse();
    }
}
