package se.tink.backend.aggregation.nxgen.core.account.creditcard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.PaymentCardNumberIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class CreditCardAccountTest {

    private final CreditCardModule CARD_MODULE =
            CreditCardModule.builder()
                    .withCardNumber("5254 - 7078 1002 9393")
                    .withBalance(ExactCurrencyAmount.of(579.3, "EUR"))
                    .withAvailableCredit(ExactCurrencyAmount.of(420.7, "EUR"))
                    .withCardAlias("Kalle Anka-Kortet")
                    .build();

    private final IdModule ID_MODULE =
            IdModule.builder()
                    .withUniqueIdentifier("5254 - 7078 1002 9393")
                    .withAccountNumber("5254 - 7078 1002 9393")
                    .withAccountName("Kalle Anka-Kortet")
                    .addIdentifier(new PaymentCardNumberIdentifier("4532 - 1012 0773 2467"))
                    .build();

    @Test(expected = NullPointerException.class)
    public void noCardDetails() {
        // Build an otherwise correct account
        CreditCardAccount.nxBuilder().withCardDetails(null).withId(ID_MODULE).build();
    }

    @Test(expected = NullPointerException.class)
    public void nullArguments() {
        CreditCardAccount.nxBuilder().withCardDetails(CARD_MODULE).withId(null).build();
    }

    @SuppressWarnings("unused")
    private static class SomeBoxing {
        private String x;
        private int y;

        private SomeBoxing() {}

        SomeBoxing(String x, int y) {
            this.x = x;
            this.y = y;
        }

        public String getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }

    @Test
    public void testSuccessfulBuild() {
        SomeBoxing box = new SomeBoxing("TestString", 15);

        CreditCardAccount account =
                CreditCardAccount.nxBuilder()
                        .withCardDetails(CARD_MODULE)
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier("4532 - 1012 0773 2467")
                                        .withAccountNumber("4532 - 1012 0773 2467")
                                        .withAccountName("Kalle Anka-Kortet")
                                        .addIdentifier(
                                                new PaymentCardNumberIdentifier("4532101207732467"))
                                        .setProductName("Kalle Anka-Kortet")
                                        .build())
                        .addHolderName("Jürgen Flughaubtkopf")
                        .setApiIdentifier("2a3ffe-38320c")
                        .putInTemporaryStorage("box", box)
                        .build();

        Optional<SomeBoxing> storage = account.getFromTemporaryStorage("box", SomeBoxing.class);

        assertEquals(AccountTypes.CREDIT_CARD, account.getType());
        assertEquals("4532101207732467", account.getIdModule().getUniqueId());
        assertEquals("4532 - 1012 0773 2467", account.getAccountNumber());

        assertEquals(
                "payment-card-number://4532101207732467",
                account.getIdModule().getIdentifiers().stream()
                        .map(AccountIdentifier::toString)
                        .sorted()
                        .collect(Collectors.joining(";")));
        assertEquals("Kalle Anka-Kortet", account.getIdModule().getAccountName());
        assertEquals("Kalle Anka-Kortet", account.getIdModule().getProductName());

        assertEquals(579.3, account.getBalance().getValue(), 0);
        assertEquals("EUR", account.getBalance().getCurrency());
        assertNotNull(account.getAvailableCredit());
        assertEquals(420.7, account.getAvailableCredit().getValue(), 0);

        assertTrue(storage.isPresent());
        assertEquals("TestString", storage.get().x);
        assertEquals(15, storage.get().y);
        assertEquals("Jürgen Flughaubtkopf", account.getHolderName().toString());
        assertEquals("2a3ffe-38320c", account.getApiIdentifier());
    }
}
