package se.tink.backend.aggregation.nxgen.core.account.creditcard;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
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
                    .withBalance(ExactCurrencyAmount.of(BigDecimal.valueOf(579.3), "EUR"))
                    .withAvailableCredit(ExactCurrencyAmount.of(BigDecimal.valueOf(420.7), "EUR"))
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
        CreditCardAccount.nxBuilder()
                .withCardDetails(null)
                .withoutFlags()
                .withId(ID_MODULE)
                .build();
    }

    @Test(expected = NullPointerException.class)
    public void nullArguments() {
        CreditCardAccount.nxBuilder()
                .withCardDetails(CARD_MODULE)
                .withoutFlags()
                .withId(null)
                .build();
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
                        .withoutFlags()
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

        assertThat(account.getType()).isEqualTo(AccountTypes.CREDIT_CARD);
        assertThat(account.getIdModule().getUniqueId()).isEqualTo("4532101207732467");
        assertThat(account.getAccountNumber()).isEqualTo("4532 - 1012 0773 2467");

        assertThat(
                        account.getIdModule().getIdentifiers().stream()
                                .map(AccountIdentifier::toString)
                                .sorted()
                                .collect(Collectors.joining(";")))
                .isEqualTo("payment-card-number://4532101207732467");
        assertThat(account.getIdModule().getAccountName()).isEqualTo("Kalle Anka-Kortet");
        assertThat(account.getIdModule().getProductName()).isEqualTo("Kalle Anka-Kortet");

        assertThat(account.getExactBalance().getDoubleValue()).isEqualTo(579.3);
        assertThat(account.getExactBalance().getCurrencyCode()).isEqualTo("EUR");
        assertThat(account.getExactAvailableCredit()).isNotNull();
        assertThat(account.getExactAvailableCredit().getDoubleValue()).isEqualTo(420.7);

        assertThat(storage).isPresent();
        assertThat(storage.get().x).isEqualTo("TestString");
        assertThat(storage.get().y).isEqualTo(15);
        assertThat(account.getHolderName().toString()).isEqualTo("Jürgen Flughaubtkopf");
        assertThat(account.getApiIdentifier()).isEqualTo("2a3ffe-38320c");
    }

    @Test
    public void shouldReturnProperAccountType() {
        CreditCardAccount creditCardAccount = getTestCreditCardAccount();
        assertThat(creditCardAccount.getType()).isEqualTo(AccountTypes.CREDIT_CARD);
    }

    private CreditCardAccount getTestCreditCardAccount() {
        return CreditCardAccount.nxBuilder()
                .withCardDetails(CARD_MODULE)
                .withoutFlags()
                .withId(ID_MODULE)
                .build();
    }
}
