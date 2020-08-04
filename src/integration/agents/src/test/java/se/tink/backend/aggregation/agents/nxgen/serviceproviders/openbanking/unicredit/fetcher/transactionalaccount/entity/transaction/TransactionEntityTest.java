package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.entity.transaction;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.Properties;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class TransactionEntityTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    public void toTinkTransaction() {
        // given
        TransactionEntity entity = transactionAsJson(transactionEntityProps("123.45"));

        // when
        Transaction result = entity.toTinkTransaction(false);

        // then
        assertThat(result.getDescription()).isEqualTo("test-remittance-information-unstructured");
        assertThat(result.getExactAmount().getExactValue()).isEqualTo(new BigDecimal("123.45"));
        assertThat(result.isPending()).isFalse();
    }

    private Properties transactionEntityProps(final String amount) {
        Properties transaction = new Properties();
        transaction.setProperty("bookingDate", "2019-10-11");
        transaction.setProperty(
                "remittanceInformationUnstructured", "test-remittance-information-unstructured");

        Properties transactionAmount = new Properties();
        transactionAmount.setProperty("amount", amount);
        transactionAmount.setProperty("currency", "EUR");
        transaction.put("transactionAmount", transactionAmount);

        return transaction;
    }

    private static TransactionEntity transactionAsJson(final Properties transaction) {
        Gson gsonObj = new Gson();
        try {
            return OBJECT_MAPPER.readValue(gsonObj.toJson(transaction), TransactionEntity.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
