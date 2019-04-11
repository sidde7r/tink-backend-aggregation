package se.tink.backend.aggregation.agents.creditcards.coop.v2.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.assertj.core.data.Offset;
import org.junit.Rule;
import org.junit.Test;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.libraries.social.security.time.SwedishTimeRule;

public class TransactionEntityTest {
    private static final String transaction =
            "{\"Amount\":-159,\"Date\":\"/Date(1480806000000+0100)/\",\"Text\":\"DELIBRUKET FLATBREAD PARA\"}";

    @Rule public SwedishTimeRule timeRule = new SwedishTimeRule();

    @Test
    public void deserialize() throws IOException {
        TransactionEntity transactionEntity =
                new ObjectMapper().readValue(transaction, TransactionEntity.class);

        assertThat(transactionEntity.getAmount()).isEqualTo(-159.0, Offset.offset(0.0001));
        assertThat(transactionEntity.getDate()).isEqualTo("/Date(1480806000000+0100)/");
        assertThat(transactionEntity.getDescription()).isEqualTo("DELIBRUKET FLATBREAD PARA");
    }

    @Test
    public void testToTransaction() {
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setAmount(-159.0);
        transactionEntity.setDate("/Date(1480806000000+0100)/");
        transactionEntity.setDescription("Posten TEST");

        Transaction transaction = transactionEntity.toTransaction();

        assertThat(transaction.getAmount()).isEqualTo(-159.0, Offset.offset(0.0001));
        assertThat(transaction.getDate())
                .hasYear(2016)
                .hasMonth(12)
                .hasDayOfMonth(4)
                .hasHourOfDay(12)
                .hasMinute(0)
                .hasSecond(0)
                .hasMillisecond(0);
        assertThat(transaction.getDescription()).isEqualTo("Posten TEST");
    }

    @Test
    public void testToTransactionShorterTimestamp() {
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setAmount(-159.0);
        transactionEntity.setDate("/Date(982710000000+0100)/");
        transactionEntity.setDescription("Posten TEST");

        Transaction transaction = transactionEntity.toTransaction();

        assertThat(transaction.getDate())
                .hasYear(2001)
                .hasMonth(2)
                .hasDayOfMonth(21)
                .hasHourOfDay(12)
                .hasMinute(0)
                .hasSecond(0)
                .hasMillisecond(0);
    }
}
