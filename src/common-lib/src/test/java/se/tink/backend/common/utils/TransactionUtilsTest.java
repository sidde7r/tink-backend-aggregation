package se.tink.backend.common.utils;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionTypes;

@RunWith(JUnitParamsRunner.class)
public class TransactionUtilsTest {
    @Test
    @Parameters({
            "46735075020", // SEB
            "Swish mottagen +46735075020", // Swedbank
            "Swish skickad +46735075020", // Swedbank
            "Swish Fredrik Cedervall", // ICA Banken
            "Swish inbetalning Fredrik Cedervall", // Nordea
            "Swish betalning Fredrik Cedervall", // Nordea
            "Swish från Fredrik Cedervall", // Skandiabanken
            "Swish till Fredrik Cedervall", // Skandiabanken
            "Swish till Cedervall\\, Fredrik", // Skandiabanken
            //"Fredrik Cedervall", // Handelsbanken
    })
    public void testSwish(String description) {
        Transaction transaction = new Transaction();
        transaction.setOriginalDescription(description);
        transaction.setType(TransactionTypes.TRANSFER);

        Assert.assertTrue(TransactionUtils.isSwish(transaction));
    }
}
