package se.tink.backend.aggregation.nxgen.core.transaction;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class AggregationTransactionTest {

    @Test
    public void getPayloadTest() {
        Map<TransactionPayloadTypes, String> payloads = new HashMap<>();
        payloads.put(TransactionPayloadTypes.DETAILS, "details");
        Transaction transaction =
                new Transaction(
                        new ExactCurrencyAmount(BigDecimal.valueOf(1.00), "EUR"),
                        new Date(),
                        "Description",
                        false,
                        "rawDetails",
                        "externalId",
                        TransactionTypes.PAYMENT,
                        payloads);

        assertEquals("details", transaction.getPayload().get(TransactionPayloadTypes.DETAILS));
    }
}
