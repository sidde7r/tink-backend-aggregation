package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.entity;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class ArkeaTransactionEntityTest {

    @Test
    public void shouldMapToTinkTransactionWithUnstructuredRemittanceInformation()
            throws IOException {
        // when
        ArkeaTransactionEntity transactionEntity =
                SerializationUtils.deserializeFromString(
                        "{\"transactionAmount\": {\"amount\": 12.25,\"currency\": \"EUR\"},\"creditDebitIndicator\": \"CDRT\",\"status\": \"BOOK\",\"bookingDate\": \"2018-11-05T00:00:00\",\"valueDate\": \"2018-11-05T00:00:00\",\"transactionDate\": \"2018-11-05T00:00:00\",\"remittanceInformation\": {\"unstructured\": [\"DESCRIPTION\"]}}",
                        ArkeaTransactionEntity.class);

        Transaction tinkTransaction = transactionEntity.toTinkTransaction();

        // then
        assertThat(tinkTransaction.getExactAmount().getDoubleValue()).isEqualTo(12.25);
        assertThat(tinkTransaction.isPending()).isFalse();
        assertThat(tinkTransaction.getDate()).isEqualToIgnoringHours("2018-11-05");
        assertThat(tinkTransaction.getDescription()).isEqualTo("DESCRIPTION");
    }
}
