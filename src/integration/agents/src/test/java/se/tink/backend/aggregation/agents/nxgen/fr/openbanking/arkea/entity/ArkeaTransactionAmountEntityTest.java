package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.entity;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class ArkeaTransactionAmountEntityTest {

    private ArkeaTransactionAmountEntity transactionAmountEntity;
    private String creditDebitIndicator;

    @Before
    public void setUp() throws IOException {
        transactionAmountEntity =
                SerializationUtils.deserializeFromString(
                        "{\n"
                                + "    \"amount\": \"12.25\",\n"
                                + "    \"currency\": \"EUR\"\n"
                                + "}",
                        ArkeaTransactionAmountEntity.class);
    }

    @Test
    public void shouldReturnPositiveTransactionAmount() {
        // given
        ArkeaTransactionEntity transactionEntity = createTransactionEntity("CRDT");
        creditDebitIndicator = transactionEntity.getCreditDebitIndicator();
        ExactCurrencyAmount expectedTransactionAmount = ExactCurrencyAmount.of(12.25, "EUR");
        // when
        ExactCurrencyAmount transactionAmount =
                transactionAmountEntity.getAmount(creditDebitIndicator);

        // then
        assertThat(transactionAmount).isEqualTo(expectedTransactionAmount);
    }

    @Test
    public void shouldReturnNegativeTransactionAmount() {
        // given
        ArkeaTransactionEntity transactionEntity = createTransactionEntity("DBIT");
        creditDebitIndicator = transactionEntity.getCreditDebitIndicator();
        ExactCurrencyAmount expectedTransactionAmount =
                ExactCurrencyAmount.of(12.25, "EUR").negate();

        // when
        ExactCurrencyAmount transactionAmount =
                transactionAmountEntity.getAmount(creditDebitIndicator);

        // then
        assertThat(transactionAmount).isEqualTo(expectedTransactionAmount);
    }

    private ArkeaTransactionEntity createTransactionEntity(String creditDebitIndicator) {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"entryReference\": \"AF5T2\",\n"
                        + "    \"transactionAmount\": {\n"
                        + "        \"amount\": \"12.25\",\n"
                        + "        \"currency\": \"EUR\"\n"
                        + "    },\n"
                        + "    \"creditDebitIndicator\": \""
                        + creditDebitIndicator
                        + "\" ,\n"
                        + "    \"status\": \"BOOK\",\n"
                        + "    \"bookingDate\": \"2018-02-12\",\n"
                        + "    \"remittanceInformation\": {\n"
                        + "        \"unstructured\": [\n"
                        + "            \"SEPA CREDIT TRANSFER from PSD2Company\"\n"
                        + "        ]\n"
                        + "    }\n"
                        + "}",
                ArkeaTransactionEntity.class);
    }
}
