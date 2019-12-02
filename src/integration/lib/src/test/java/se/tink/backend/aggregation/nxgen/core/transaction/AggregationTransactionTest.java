package se.tink.backend.aggregation.nxgen.core.transaction;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.libraries.amount.ExactCurrencyAmount;

class AggregationTransactionTest {

    @Test
    void getPayloadTest() {
        Map<TransactionPayloadTypes, String> payloads = new HashMap<>();
        payloads.put(TransactionPayloadTypes.DETAILS, "details");
        Transaction transaction =
                new Transaction(
                        new ExactCurrencyAmount(new BigDecimal(1.00), "EUR"),
                        new Date(),
                        "Description",
                        false,
                        "rawDetails",
                        "externalId",
                        TransactionTypes.PAYMENT,
                        payloads);

        Assert.assertEquals(
                "details", transaction.getPayload().get(TransactionPayloadTypes.DETAILS));
    }
}
