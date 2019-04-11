package se.tink.backend.aggregation.agents.abnamro.ics.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.backend.aggregation.agents.abnamro.client.model.AmountEntity;
import se.tink.backend.aggregation.agents.abnamro.client.model.creditcards.TransactionContainerEntity;
import se.tink.backend.aggregation.agents.abnamro.client.model.creditcards.TransactionEntity;

public class TransactionMapperTest {
    @Test
    public void testTransactionIncome() throws Exception {
        TransactionContainerEntity entity = new TransactionContainerEntity();

        TransactionEntity transactionEntity = new TransactionEntity();

        AmountEntity amountEntity = new AmountEntity();

        // This is an income since ABN has inverted the sign
        amountEntity.setAmount(-100D);
        amountEntity.setCurrencyCode("SEK");

        transactionEntity.setBillingAmount(amountEntity);

        entity.setCreditCardTransaction(transactionEntity);

        assertThat(TransactionMapper.toTransaction(entity).getAmount()).isEqualTo(100D);
    }

    @Test
    public void testTransactionExpense() throws Exception {
        TransactionContainerEntity entity = new TransactionContainerEntity();

        TransactionEntity transactionEntity = new TransactionEntity();

        AmountEntity amountEntity = new AmountEntity();

        // This is an expense since ABN has inverted the sign
        amountEntity.setAmount(100D);
        amountEntity.setCurrencyCode("SEK");

        transactionEntity.setBillingAmount(amountEntity);

        entity.setCreditCardTransaction(transactionEntity);

        assertThat(TransactionMapper.toTransaction(entity).getAmount()).isEqualTo(-100D);
    }
}
