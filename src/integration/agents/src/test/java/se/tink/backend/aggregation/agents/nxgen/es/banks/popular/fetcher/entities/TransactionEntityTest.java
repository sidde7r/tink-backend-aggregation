package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.entities;

import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class TransactionEntityTest {

    @Test
    public void ensureMerchantDescriptionsAreModified() {
        String descriptionString1 = "Pago con 4B ...1234 1234, a las 01.01 h, en SPOTIFY";
        String descriptionString2 = "Pago con 4B ...1234 1234, a las 01.01 h, en SPOTIFY STOCKHOLM";

        Transaction t1 = createTransactionEntityWithDescription(descriptionString1);
        Transaction t2 = createTransactionEntityWithDescription(descriptionString2);

        Assert.assertEquals("SPOTIFY", t1.getDescription());
        Assert.assertEquals("SPOTIFY STOCKHOLM", t2.getDescription());
    }

    @Test
    public void ensureTransferDescriptionIsNotModified() {
        String descriptionString1 = "Transferencia de FIRSTNAME LASTNAME";

        Transaction t1 = createTransactionEntityWithDescription(descriptionString1);

        Assert.assertEquals(descriptionString1, t1.getDescription());

    }

    private Transaction createTransactionEntityWithDescription(String description) {
        TransactionEntity te = new TransactionEntity();
        te.setDescription(description);

        return te.toTinkTransaction();
    }
}
