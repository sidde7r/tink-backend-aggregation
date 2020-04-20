package se.tink.backend.aggregation.utils.transfer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.junit.Test;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
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

    @Test
    public void testIntraBankTransferForIbanWithSameBankCode() {
        Iban sourceIban =
                new Iban.Builder().countryCode(CountryCode.FR).bankCode("30006").buildRandom();
        Iban destIban =
                new Iban.Builder().countryCode(CountryCode.FR).bankCode("30006").buildRandom();
        AccountIdentifier sourceAccount = new IbanIdentifier(sourceIban.toString());
        AccountIdentifier destAccount = new IbanIdentifier(destIban.toString());
        assertTrue(IntraBankTransferChecker.isIbanIntraBankTransfer(sourceAccount, destAccount));
    }

    @Test
    public void testIntraBankTransferForIbanWithDifferentBankCode() {
        Iban sourceIban =
                new Iban.Builder().countryCode(CountryCode.FR).bankCode("30006").buildRandom();
        Iban destIban =
                new Iban.Builder().countryCode(CountryCode.FR).bankCode("30007").buildRandom();
        AccountIdentifier sourceAccount = new IbanIdentifier(sourceIban.toString());
        AccountIdentifier destAccount = new IbanIdentifier(destIban.toString());
        assertFalse(IntraBankTransferChecker.isIbanIntraBankTransfer(sourceAccount, destAccount));
    }

    @Test
    public void testIntraBankTransfer() {
        AccountIdentifier sourceAccount = new SwedishIdentifier("52871111111");
        AccountIdentifier destinationAccount = new SwedishIdentifier("33001111111");
        assertFalse(
                IntraBankTransferChecker.isIntraBankTransfer(sourceAccount, destinationAccount));
        sourceAccount = new SwedishIdentifier("30001111111");
        destinationAccount = new SwedishIdentifier("33001111111");

        assertTrue(IntraBankTransferChecker.isIntraBankTransfer(sourceAccount, destinationAccount));
        Iban sourceIban =
                new Iban.Builder().countryCode(CountryCode.SE).bankCode("500").buildRandom();
        Iban destIban =
                new Iban.Builder().countryCode(CountryCode.SE).bankCode("500").buildRandom();
        sourceAccount = new IbanIdentifier(sourceIban.toString());
        destinationAccount = new IbanIdentifier(destIban.toString());
        assertTrue(
                IntraBankTransferChecker.isIbanIntraBankTransfer(
                        sourceAccount, destinationAccount));

        sourceIban = new Iban.Builder().countryCode(CountryCode.SE).bankCode("500").buildRandom();
        destIban = new Iban.Builder().countryCode(CountryCode.SE).bankCode("501").buildRandom();
        sourceAccount = new IbanIdentifier(sourceIban.toString());
        destinationAccount = new IbanIdentifier(destIban.toString());
        assertFalse(
                IntraBankTransferChecker.isIbanIntraBankTransfer(
                        sourceAccount, destinationAccount));
    }
}
