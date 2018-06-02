package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.creditcard.entities;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class BankIdentifierTest {
    @Test
    public void testCreateBankIdentifier() throws Exception {
        BankIdentifier bankIdentifier = new BankIdentifier("cardNumberGuid", "kidGuid");

        assertEquals("cardNumberGuid\nkidGuid", bankIdentifier.getBankIdentifier());
    }
    @Test
    public void testParseBankIdentifier() throws Exception {
        BankIdentifier bankIdentifier = new BankIdentifier("cardNumberGuid\nkidGuid");

        assertEquals("cardNumberGuid", bankIdentifier.getCardNumberGuid());
        assertEquals("kidGuid", bankIdentifier.getKidGuid());
    }
}