package se.tink.backend.aggregation.agents.nxgen.se.creditcards.amex.fetcher.rpc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.amex.v62.AmericanExpressV62SEConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc.TimelineResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class TimeLineResponseTest {

    private String timelineData;

    @Before
    public void setup() throws IOException {
        timelineData =
                new String(
                        Files.readAllBytes(Paths.get("data/test/agents/amex/timelinedata.json")));
    }

    @Test
    public void testTimeLineResponseParsing() {
        TimelineResponse response =
                SerializationUtils.deserializeFromString(timelineData, TimelineResponse.class);
        Set<Transaction> transactions =
                response.getPendingTransactions(new AmericanExpressV62SEConfiguration(), "00");
        Assert.assertFalse(transactions.isEmpty());
        Assert.assertEquals(2, transactions.size());
    }
}
