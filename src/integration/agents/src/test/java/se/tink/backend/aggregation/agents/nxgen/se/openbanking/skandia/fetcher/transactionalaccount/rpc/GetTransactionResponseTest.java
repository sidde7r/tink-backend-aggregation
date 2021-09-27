package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.fetcher.transactionalaccount.rpc;

import agents_platform_agents_framework.org.springframework.test.util.ReflectionTestUtils;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.fetcher.transactionalaccount.entities.TransactionsEntity;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class GetTransactionResponseTest {

    @Test
    public void shouldReturnEmptyTransactionsWhenEmptyTransactionsResponse() {
        // given
        GetTransactionsResponse transactionsResponse = new GetTransactionsResponse();
        ReflectionTestUtils.setField(transactionsResponse, "transactions", getEmptyTransactions());

        // then
        Assert.assertEquals(Collections.emptyList(), transactionsResponse.getTinkTransactions());
    }

    private TransactionsEntity getEmptyTransactions() {
        return SerializationUtils.deserializeFromString(
                "{\"pending\":[],\"booked\":[]}\n", TransactionsEntity.class);
    }
}
