package se.tink.backend.aggregation.agents.banks.lansforsakringar;

import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.Originator;
import se.tink.libraries.account.enums.AccountIdentifierType;

public class OriginatorTest {

    @Test
    public void testValidBankGiroAccountNumber() {
        Originator o = createOriginator("687-5496", "TINK AB", null);
        Assert.assertEquals(AccountIdentifierType.SE_BG, o.generalGetAccountIdentifier().getType());
    }

    @Test
    public void testValidPostGiroAccountNumber() {
        Originator o = createOriginator("687549-6", "TINK AB", null);
        Assert.assertEquals(AccountIdentifierType.SE_PG, o.generalGetAccountIdentifier().getType());
    }

    @Test
    public void testInvalidBankGiroAccountNumber() {
        Originator o1 = createOriginator("BankGiro 687-5496", "TINK AB", null);
        Assert.assertFalse(o1.generalGetAccountIdentifier().isValid());

        Originator o2 = createOriginator("87-5496", "TINK AB", null);
        Assert.assertFalse(o2.generalGetAccountIdentifier().isValid());

        Originator o3 = createOriginator("6875496", "TINK AB", null);
        Assert.assertFalse(o3.generalGetAccountIdentifier().isValid());
    }

    @Test
    public void testInvalidPostGiroAccountNumber() {
        Originator o1 = createOriginator("PostGiro 687549-6", "TINK AB", null);
        Assert.assertFalse(o1.generalGetAccountIdentifier().isValid());

        Originator o2 = createOriginator("8754-96", "TINK AB", null);
        Assert.assertFalse(o2.generalGetAccountIdentifier().isValid());

        Originator o3 = createOriginator("6875496", "TINK AB", null);
        Assert.assertFalse(o3.generalGetAccountIdentifier().isValid());
    }

    private Originator createOriginator(String number, String name, String type) {
        Originator o = new Originator();
        o.setGiroNumber(number);
        o.setName(name);
        o.setOcrType(type);

        return o;
    }
}
