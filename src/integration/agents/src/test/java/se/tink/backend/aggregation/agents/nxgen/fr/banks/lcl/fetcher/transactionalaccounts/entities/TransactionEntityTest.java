package se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.fetcher.transactionalaccounts.entities;

import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class TransactionEntityTest {
    private static final String transactionDescr1 = "CB MERCHANT NAME AB 01/01/18";
    private static final String transactionDescr2 = "CB  MERCHANT NAME      01/01/18";

    @Test
    public void ensureMerchantDescriptionsAreModified() {
        Transaction t1 = createTransactionEntityWithDescription(transactionDescr1);
        Transaction t2 = createTransactionEntityWithDescription(transactionDescr2);

        Assert.assertEquals("MERCHANT NAME AB", t1.getDescription());
        Assert.assertEquals("MERCHANT NAME", t2.getDescription());
    }

    private Transaction createTransactionEntityWithDescription(String description) {
        TransactionEntity te = new TransactionEntity();

        te.setTransactionAmount("-10,10");
        te.setTransactionDate("20180101");
        te.setTransactionDescription(description);

        return te.toTinkTransaction();
    }
}
