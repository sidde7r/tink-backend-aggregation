package se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq;

import static se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.BunqAgentTestData.TRANSACTION_01;

import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.fetchers.transactional.rpc.TransactionWrapper;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BunqAgentDeserializationTest {

    @Test
    public void testTransactionDescription() {
        TransactionWrapper transactionEntity =
                SerializationUtils.deserializeFromString(TRANSACTION_01, TransactionWrapper.class);
        Transaction transaction = transactionEntity.toTinkTransaction();
        Assert.assertEquals(transaction.getDescription().trim(), "MJD MINDERHOUD");
    }
}
