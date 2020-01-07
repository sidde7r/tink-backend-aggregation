package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.cards.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import org.junit.Test;

public class TransactionAmountEntityTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    public void toTinkAmountWithNegativeAmount() throws IOException {
        // given
        String givenAmount = "-92.54";
        BigDecimal expectedAmount = new BigDecimal("-92.54");
        TransactionAmountEntity transactionAmountEntity =
                amountToTransactionAmountEntity(givenAmount);

        // when
        BigDecimal result = transactionAmountEntity.toTinkAmount().getExactValue();

        // then
        assertThat(result).isEqualTo(expectedAmount);
    }

    @Test
    public void toTinkAmountWithZeroAmount() throws IOException {
        // given
        String givenAmount = "0.0";
        BigDecimal expectedAmount = new BigDecimal("0.0");
        TransactionAmountEntity transactionAmountEntity =
                amountToTransactionAmountEntity(givenAmount);

        // when
        BigDecimal result = transactionAmountEntity.toTinkAmount().getExactValue();

        // then
        assertThat(result).isEqualTo(expectedAmount);
    }

    @Test
    public void toTinkAmountWithPositiveAmount() throws IOException {
        // given
        String givenAmount = "234.15";
        BigDecimal expectedAmount = new BigDecimal("234.15");
        TransactionAmountEntity transactionAmountEntity =
                amountToTransactionAmountEntity(givenAmount);

        // when
        BigDecimal result = transactionAmountEntity.toTinkAmount().getExactValue();

        // then
        assertThat(result).isEqualTo(expectedAmount);
    }

    private static TransactionAmountEntity amountToTransactionAmountEntity(final String amount)
            throws IOException {
        return OBJECT_MAPPER.readValue(
                "{\"amount\":" + amount + ", \"currency\": \"EUR\"}",
                TransactionAmountEntity.class);
    }
}
