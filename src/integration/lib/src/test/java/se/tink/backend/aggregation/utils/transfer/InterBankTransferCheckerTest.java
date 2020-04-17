package se.tink.backend.aggregation.utils.transfer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

public class InterBankTransferCheckerTest {

    @Test
    public void ensureForSebBank() {
        AccountIdentifier sourceAccount = new SwedishIdentifier("52871111111");
        AccountIdentifier destinationAccount = new SwedishIdentifier("52031111111");
        assertTrue(
                IntraBankTransferChecker.isSwedishMarketIntraBankTransfer(
                        sourceAccount, destinationAccount));
    }

    @Test
    public void ensureForNordeaPersonKontoBank() {
        AccountIdentifier sourceAccount = new SwedishIdentifier("30001111111");
        AccountIdentifier destinationAccount = new SwedishIdentifier("33001111111");
        assertTrue(
                IntraBankTransferChecker.isSwedishMarketIntraBankTransfer(
                        sourceAccount, destinationAccount));
    }

    @Test
    public void ensureForInterBank() {
        AccountIdentifier sourceAccount = new SwedishIdentifier("52871111111");
        AccountIdentifier destinationAccount = new SwedishIdentifier("33001111111");
        assertFalse(
                IntraBankTransferChecker.isSwedishMarketIntraBankTransfer(
                        sourceAccount, destinationAccount));
    }

    @Test
    public void ensureForBg() {
        AccountIdentifier sourceAccount = new SwedishIdentifier("52871111111");
        AccountIdentifier destinationAccount = new BankGiroIdentifier("3611803");
        assertFalse(
                IntraBankTransferChecker.isSwedishMarketIntraBankTransfer(
                        sourceAccount, destinationAccount));
    }
}
