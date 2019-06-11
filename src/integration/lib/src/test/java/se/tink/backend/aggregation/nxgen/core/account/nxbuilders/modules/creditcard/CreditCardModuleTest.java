package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CreditCardModuleTest {

    @Test(expected = NullPointerException.class)
    public void nullArguments() {
        CreditCardModule.builder().withCardNumber(null).withCardAlias(null).build();
    }

    @Test(expected = NullPointerException.class)
    public void missingCardNumber() {
        CreditCardModule.builder()
                .withCardNumber(null)
                .withCardAlias("PRO MerVärde MasterCard")
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidCardNumber() {
        CreditCardModule.builder()
                .withCardNumber("ABCD-0000-BBBB-DDDD")
                .withCardAlias("PRO MerVärde MasterCard")
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void tooShortCardNumber() {
        CreditCardModule.builder()
                .withCardNumber("0-00-1-11-2-22")
                .withCardAlias("PRO MerVärde MasterCard")
                .build();
    }

    @Test(expected = NullPointerException.class)
    public void missingCardAlias() {
        CreditCardModule.builder()
                .withCardNumber("0000-1111-2222-3333")
                .withCardAlias(null)
                .build();
    }

    @Test
    public void workingBuild() {
        String cardNumber = "5254 - 9600 7078 9002";
        String cardAlias = "PRO MerVärde MasterCard";

        CreditCardModule cardModule =
                CreditCardModule.builder()
                        .withCardNumber(cardNumber)
                        .withCardAlias(cardAlias)
                        .build();

        assertEquals("5254960070789002", cardModule.getCardNumber());
        assertEquals(cardAlias, cardModule.getCardAlias());
    }
}
