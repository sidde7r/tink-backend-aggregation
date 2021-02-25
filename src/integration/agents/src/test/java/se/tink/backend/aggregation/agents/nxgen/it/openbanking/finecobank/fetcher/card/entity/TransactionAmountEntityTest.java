package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.card.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import org.junit.Test;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class TransactionAmountEntityTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    public void toTinkAmountWithNegativeAmount() throws IOException {
        // given
        String givenAmount = "-92.54";
        String givenCurrency = "EUR";
        // and
        TransactionAmountEntity transactionAmountEntity =
                amountToTransactionAmountEntity(givenAmount, givenCurrency);
        // and
        ExactCurrencyAmount expectedAmount =
                new ExactCurrencyAmount(new BigDecimal(givenAmount).negate(), givenCurrency);

        // when
        ExactCurrencyAmount result = transactionAmountEntity.toTinkAmount();

        // then
        assertThat(result).isEqualTo(expectedAmount);
    }

    @Test
    public void toTinkAmountWithZeroAmount() throws IOException {
        // given
        String givenAmount = "0.0";
        String givenCurrency = "EUR";
        // and
        TransactionAmountEntity transactionAmountEntity =
                amountToTransactionAmountEntity(givenAmount, givenCurrency);
        // and
        ExactCurrencyAmount expectedAmount =
                new ExactCurrencyAmount(new BigDecimal(givenAmount).negate(), givenCurrency);

        // when
        ExactCurrencyAmount result = transactionAmountEntity.toTinkAmount();

        // then
        assertThat(result).isEqualTo(expectedAmount);
    }

    @Test
    public void toTinkAmountWithPositiveAmount() throws IOException {
        // given
        String givenAmount = "234.15";
        String givenCurrency = "EUR";
        // and
        TransactionAmountEntity transactionAmountEntity =
                amountToTransactionAmountEntity(givenAmount, givenCurrency);
        // and
        ExactCurrencyAmount expectedAmount =
                new ExactCurrencyAmount(new BigDecimal(givenAmount).negate(), givenCurrency);

        // when
        ExactCurrencyAmount result = transactionAmountEntity.toTinkAmount();

        // then
        assertThat(result).isEqualTo(expectedAmount);
    }

    private static TransactionAmountEntity amountToTransactionAmountEntity(
            final String amount, final String currency) throws IOException {
        return OBJECT_MAPPER.readValue(
                "{\"amount\":" + amount + ", \"currency\": \"" + currency + "\"}",
                TransactionAmountEntity.class);
    }
}
