package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class CreditCardModuleTest {

    @Test(expected = NullPointerException.class)
    public void nullArguments() {
        CreditCardModule.builder()
                .withCardNumber(null)
                .withBalance(null)
                .withAvailableCredit(null)
                .withCardAlias(null)
                .build();
    }

    @Test(expected = NullPointerException.class)
    public void missingCardNumber() {
        CreditCardModule.builder()
                .withCardNumber(null)
                .withBalance(ExactCurrencyAmount.of(233, "SEK"))
                .withAvailableCredit(ExactCurrencyAmount.of(487, "SEK"))
                .withCardAlias("PRO MerVärde MasterCard")
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidCardNumber() {
        CreditCardModule.builder()
                .withCardNumber("ABC")
                .withBalance(ExactCurrencyAmount.of(233, "SEK"))
                .withAvailableCredit(ExactCurrencyAmount.of(487, "SEK"))
                .withCardAlias("PRO MerVärde MasterCard")
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void tooShortCardNumber() {
        CreditCardModule.builder()
                .withCardNumber("******123")
                .withBalance(ExactCurrencyAmount.of(233, "SEK"))
                .withAvailableCredit(ExactCurrencyAmount.of(487, "SEK"))
                .withCardAlias("PRO MerVärde MasterCard")
                .build();
    }

    @Test(expected = NullPointerException.class)
    public void missingCardAlias() {
        CreditCardModule.builder()
                .withCardNumber("0000-1111-2222-3333")
                .withBalance(ExactCurrencyAmount.of(233, "SEK"))
                .withAvailableCredit(ExactCurrencyAmount.of(487, "SEK"))
                .withCardAlias(null)
                .build();
    }

    @Test
    public void workingBuild() {
        String cardNumber = "5254 9*** **** 9002";
        String cardAlias = "PRO MerVärde MasterCard";

        CreditCardModule cardModule =
                CreditCardModule.builder()
                        .withCardNumber(cardNumber)
                        .withBalance(ExactCurrencyAmount.of(233, "SEK"))
                        .withAvailableCredit(ExactCurrencyAmount.of(487, "SEK"))
                        .withCardAlias(cardAlias)
                        .build();

        assertEquals("5254 9*** **** 9002", cardModule.getCardNumber());
        assertEquals(cardAlias, cardModule.getCardAlias());
    }
}
