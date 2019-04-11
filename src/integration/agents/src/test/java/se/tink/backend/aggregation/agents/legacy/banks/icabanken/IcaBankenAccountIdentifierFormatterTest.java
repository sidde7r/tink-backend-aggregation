package se.tink.backend.aggregation.agents.banks.icabanken;

import org.junit.Assert;
import org.junit.Test;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

public class IcaBankenAccountIdentifierFormatterTest {

    @Test
    public void testIcaBankenToSavingsbank() {
        Assert.assertEquals(
                "842280033171950",
                new SwedishIdentifier("8422833171950")
                        .getIdentifier(new IcaBankenAccountIdentifierFormatter()));
    }

    @Test
    public void testIcaBankenToSwedbank() {
        Assert.assertEquals(
                "832799130676811",
                new SwedishIdentifier("832799130676811")
                        .getIdentifier(new IcaBankenAccountIdentifierFormatter()));
    }

    @Test
    public void testIcaBankenToSwedbankLongNumber() {
        Assert.assertEquals(
                "810590744435694",
                new SwedishIdentifier("81059744435694")
                        .getIdentifier(new IcaBankenAccountIdentifierFormatter()));
    }

    @Test
    public void testNotHandledBank() {
        Assert.assertEquals(
                "90234722659",
                new SwedishIdentifier("90234722659")
                        .getIdentifier(new IcaBankenAccountIdentifierFormatter()));
    }
}
