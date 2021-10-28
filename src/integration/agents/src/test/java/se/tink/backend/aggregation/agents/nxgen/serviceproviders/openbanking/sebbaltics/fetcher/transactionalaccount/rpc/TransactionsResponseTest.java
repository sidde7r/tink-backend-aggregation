package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.fetcher.transactionalaccount.rpc;

import agents_platform_agents_framework.org.springframework.test.util.ReflectionTestUtils;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.fetcher.transactionalaccount.entities.TransactionsEntity;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class TransactionsResponseTest {

    @Test
    public void shouldReturnEmptyListWhenEmptyTransactionsResponse() {
        // given
        TransactionsResponse transactionsResponse = new TransactionsResponse();
        ReflectionTestUtils.setField(transactionsResponse, "transactions", getEmptyTransactions());

        // then
        Assert.assertEquals(
                Collections.emptyList(), transactionsResponse.getTinkTransactions("market"));
    }

    private TransactionsEntity getEmptyTransactions() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"pending\": [\n"
                        + "    ],\n"
                        + "    \"booked\": [\n"
                        + "    ]\n"
                        + "  }",
                TransactionsEntity.class);
    }
}
