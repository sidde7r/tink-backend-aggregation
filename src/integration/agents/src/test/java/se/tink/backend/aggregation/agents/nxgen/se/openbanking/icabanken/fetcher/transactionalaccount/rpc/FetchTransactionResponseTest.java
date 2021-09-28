package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.fetcher.transactionalaccount.rpc;

import agents_platform_agents_framework.org.springframework.test.util.ReflectionTestUtils;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.fetcher.transactionalaccount.entity.transaction.TransactionsEntity;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class FetchTransactionResponseTest {

    @Test
    public void shouldReturnEmptyTransactionsWhenEmptyTransactionsResponse() {
        // given
        FetchTransactionsResponse transactionsResponse = new FetchTransactionsResponse();
        ReflectionTestUtils.setField(transactionsResponse, "transactions", getEmptyTransactions());

        // then
        Assert.assertEquals(Collections.emptyList(), transactionsResponse.getTinkTransactions());
    }

    private TransactionsEntity getEmptyTransactions() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "\t\t\"booked\": [\n"
                        + "],\n"
                        + "\t\t\"pending\": [\n"
                        + "\t\t]\n"
                        + "\t},",
                TransactionsEntity.class);
    }
}
