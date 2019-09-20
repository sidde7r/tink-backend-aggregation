package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.creditcard.entities;

import java.math.BigDecimal;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CreditCardTransactionsTest {

    @Test
    public void testCreditCardTransactionParsing() {
        CreditCardTransactionEntity transaction =
                SerializationUtils.deserializeFromString(
                        CreditCardEntityTestData.CREDIT_CARD_TRANSACTION,
                        CreditCardTransactionEntity.class);

        CreditCardTransaction trx = transaction.toTinkTransaction();
        Assert.assertEquals(BigDecimal.valueOf(-509.21), trx.getExactAmount().getExactValue());
        Assert.assertEquals("EUR", trx.getExactAmount().getCurrencyCode());
        Assert.assertEquals("NORDIC LIGHT HOTEL CNP", trx.getDescription());
    }
}
