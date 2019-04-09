package se.tink.libraries.account.identifiers.formatters;

import static org.junit.Assert.*;

import org.junit.Test;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.PlusGiroIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

public class DisplayAccountIdentifierFormatterTest {

    @Test(expected = IllegalArgumentException.class)
    public void expectIllegalArgumentExcpetionOnNonValidSwedishIdentifier() {
        AccountIdentifier nonValid = new SwedishIdentifier("123");
        nonValid.getIdentifier(new DisplayAccountIdentifierFormatter());
    }

    @Test(expected = IllegalArgumentException.class)
    public void expectIllegalArgumentExcpetionOnNonValidBgIdentifier() {
        AccountIdentifier nonValid = new BankGiroIdentifier("1");
        nonValid.getIdentifier(new DisplayAccountIdentifierFormatter());
    }

    @Test
    public void testValidSEAccounts() {
        AccountIdentifierFormatter formatter = new DisplayAccountIdentifierFormatter();

        AccountIdentifier se1 = new SwedishIdentifier("6152135538858");
        AccountIdentifier se2 = new SwedishIdentifier("8422831270465");

        assertEquals("6152-135538858", se1.getIdentifier(formatter));
        assertEquals("8422-8,31270465", se2.getIdentifier(formatter));
    }

    @Test
    public void testValigBGs() {
        AccountIdentifierFormatter formatter = new DisplayAccountIdentifierFormatter();

        AccountIdentifier bg1 = new BankGiroIdentifier("1234567");
        AccountIdentifier bg2 = new BankGiroIdentifier("12345678");

        assertEquals("123-4567", bg1.getIdentifier(formatter));
        assertEquals("1234-5678", bg2.getIdentifier(formatter));
    }

    @Test
    public void voidTestBankGiro1() {
        AccountIdentifierFormatter formatter = new DisplayAccountIdentifierFormatter();

        AccountIdentifier identifier = new BankGiroIdentifier("7308596");
        assertEquals("730-8596", identifier.getIdentifier(formatter));
    }

    @Test
    public void voidTestBankGiro2() {
        AccountIdentifierFormatter formatter = new DisplayAccountIdentifierFormatter();

        AccountIdentifier identifier = new BankGiroIdentifier("76308596");
        assertEquals("7630-8596", identifier.getIdentifier(formatter));
    }

    @Test
    public void voidTestPlusGiro1() {
        AccountIdentifierFormatter formatter = new DisplayAccountIdentifierFormatter();

        AccountIdentifier identifier = new PlusGiroIdentifier("8844235");
        assertEquals("884423-5", identifier.getIdentifier(formatter));
    }
}
