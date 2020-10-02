package se.tink.backend.aggregation.utils.accountidentifier;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.junit.Test;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

public class InterBankCheckerTest {

    @Test
    public void ensureForSebBank() {
        AccountIdentifier sourceAccount = new SwedishIdentifier("52871111111");
        AccountIdentifier destinationAccount = new SwedishIdentifier("52031111111");
        assertTrue(IntraBankChecker.isSwedishMarketIntraBank(sourceAccount, destinationAccount));
    }

    @Test
    public void ensureForNordeaPersonKontoBank() {
        AccountIdentifier sourceAccount = new SwedishIdentifier("30001111111");
        AccountIdentifier destinationAccount = new SwedishIdentifier("33001111111");
        assertTrue(IntraBankChecker.isSwedishMarketIntraBank(sourceAccount, destinationAccount));
    }

    @Test
    public void ensureForInterBank() {
        AccountIdentifier sourceAccount = new SwedishIdentifier("52871111111");
        AccountIdentifier destinationAccount = new SwedishIdentifier("33001111111");
        assertFalse(IntraBankChecker.isSwedishMarketIntraBank(sourceAccount, destinationAccount));
    }

    @Test
    public void ensureForBg() {
        AccountIdentifier sourceAccount = new SwedishIdentifier("52871111111");
        AccountIdentifier destinationAccount = new BankGiroIdentifier("3611803");
        assertFalse(IntraBankChecker.isSwedishMarketIntraBank(sourceAccount, destinationAccount));
    }

    @Test
    public void testIsSwedishMarketIntraBankWithSourceIbanForDifferentCode() {
        AccountIdentifier sourceAccount = new IbanIdentifier("SE4227442575137172812348");
        AccountIdentifier destinationAccount = new SwedishIdentifier("52871111111");
        assertFalse(IntraBankChecker.isSwedishMarketIntraBank(sourceAccount, destinationAccount));
    }

    @Test
    public void testIsSwedishMarketIntraBankWithSourceIbanForSameCode() {
        AccountIdentifier sourceAccount = new IbanIdentifier("SE8654291294413315855553");
        AccountIdentifier destinationAccount = new SwedishIdentifier("52871111111");
        assertTrue(IntraBankChecker.isSwedishMarketIntraBank(sourceAccount, destinationAccount));
    }

    @Test
    public void testIntraBankForIbanWithSameBankCode() {
        Iban sourceIban =
                new Iban.Builder().countryCode(CountryCode.FR).bankCode("30006").buildRandom();
        Iban destIban =
                new Iban.Builder().countryCode(CountryCode.FR).bankCode("30006").buildRandom();
        AccountIdentifier sourceAccount = new IbanIdentifier(sourceIban.toString());
        AccountIdentifier destAccount = new IbanIdentifier(destIban.toString());
        assertTrue(IntraBankChecker.isIbanIntraBank(sourceAccount, destAccount));
    }

    @Test
    public void testIntraBankForIbanWithDifferentBankCode() {
        Iban sourceIban =
                new Iban.Builder().countryCode(CountryCode.FR).bankCode("30006").buildRandom();
        Iban destIban =
                new Iban.Builder().countryCode(CountryCode.FR).bankCode("30007").buildRandom();
        AccountIdentifier sourceAccount = new IbanIdentifier(sourceIban.toString());
        AccountIdentifier destAccount = new IbanIdentifier(destIban.toString());
        assertFalse(IntraBankChecker.isIbanIntraBank(sourceAccount, destAccount));
    }

    @Test
    public void testIntraBank() {
        AccountIdentifier sourceAccount = new SwedishIdentifier("52871111111");
        AccountIdentifier destinationAccount = new SwedishIdentifier("33001111111");
        assertFalse(
                IntraBankChecker.isAccountIdentifierIntraBank(sourceAccount, destinationAccount));
        sourceAccount = new SwedishIdentifier("30001111111");
        destinationAccount = new SwedishIdentifier("33001111111");

        assertTrue(
                IntraBankChecker.isAccountIdentifierIntraBank(sourceAccount, destinationAccount));
        Iban sourceIban =
                new Iban.Builder().countryCode(CountryCode.SE).bankCode("500").buildRandom();
        Iban destIban =
                new Iban.Builder().countryCode(CountryCode.SE).bankCode("500").buildRandom();
        sourceAccount = new IbanIdentifier(sourceIban.toString());
        destinationAccount = new IbanIdentifier(destIban.toString());
        assertTrue(
                IntraBankChecker.isAccountIdentifierIntraBank(sourceAccount, destinationAccount));

        sourceIban = new Iban.Builder().countryCode(CountryCode.SE).bankCode("500").buildRandom();
        destIban = new Iban.Builder().countryCode(CountryCode.SE).bankCode("501").buildRandom();
        sourceAccount = new IbanIdentifier(sourceIban.toString());
        destinationAccount = new IbanIdentifier(destIban.toString());
        assertFalse(
                IntraBankChecker.isAccountIdentifierIntraBank(sourceAccount, destinationAccount));
    }
}
