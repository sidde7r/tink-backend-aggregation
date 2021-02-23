package se.tink.backend.aggregation.nxgen.core.account.transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static se.tink.backend.aggregation.nxgen.core.account.entity.Party.*;

import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.AccountBuilder;
import se.tink.backend.aggregation.nxgen.core.account.AccountHolderType;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.SepaEurIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

/**
 * This suite also serves as the test suite for {@link AccountBuilder}, since that class is abstract
 * and cannot be tested directly.
 */
public class TransactionalAccountBuilderTest {

    private final IdModule ID_MODULE =
            IdModule.builder()
                    .withUniqueIdentifier("1234")
                    .withAccountNumber("1234")
                    .withAccountName("Account")
                    .addIdentifier(new SwedishIdentifier("123456789"))
                    .build();

    @Test
    public void illegalAccountType() {
        // Build an otherwise correct account
        assertEquals(
                Optional.empty(),
                TransactionalAccount.nxBuilder()
                        .withType(TransactionalAccountType.from(AccountTypes.LOAN).orElse(null))
                        .withoutFlags()
                        .withBalance(BalanceModule.of(ExactCurrencyAmount.inEUR(2)))
                        .withId(ID_MODULE)
                        .build());
    }

    @Test(expected = NullPointerException.class)
    public void nullArguments() {
        TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withoutFlags()
                .withBalance(null)
                .withId(null)
                .build();
    }

    @Test(expected = NullPointerException.class)
    public void nullBalance() {
        TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withoutFlags()
                .withBalance(null)
                .withId(ID_MODULE)
                .build();
    }

    @Test(expected = NullPointerException.class)
    public void nullId() {
        TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withoutFlags()
                .withBalance(BalanceModule.of(ExactCurrencyAmount.inEUR(2)))
                .withId(null)
                .build();
    }

    @Test(expected = NullPointerException.class)
    public void nullFlagList() {
        TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.SAVINGS)
                .withFlags((AccountFlag) null)
                .withBalance(BalanceModule.of(ExactCurrencyAmount.inSEK(2572.28)))
                .withId(ID_MODULE)
                .build();
    }

    @Test
    public void testFlagList() {
        TransactionalAccount account =
                TransactionalAccount.nxBuilder()
                        .withType(TransactionalAccountType.SAVINGS)
                        .withFlags(
                                AccountFlag.BUSINESS,
                                AccountFlag.BUSINESS,
                                AccountFlag.MANDATE,
                                AccountFlag.PSD2_PAYMENT_ACCOUNT)
                        .withBalance(BalanceModule.of(ExactCurrencyAmount.inSEK(2572.28)))
                        .withId(ID_MODULE)
                        .build()
                        .orElse(null);

        Assert.assertArrayEquals(
                new AccountFlag[] {
                    AccountFlag.BUSINESS, AccountFlag.MANDATE, AccountFlag.PSD2_PAYMENT_ACCOUNT
                },
                account.getAccountFlags().toArray());
    }

    @Test
    public void testStorage() {
        SomeBoxing box = new SomeBoxing("TestString", 15);

        TransactionalAccount account =
                TransactionalAccount.nxBuilder()
                        .withType(TransactionalAccountType.OTHER)
                        .withoutFlags()
                        .withBalance(
                                BalanceModule.builder()
                                        .withBalance(ExactCurrencyAmount.inSEK(398.32))
                                        .setInterestRate(0.0015)
                                        .setAvailableCredit(ExactCurrencyAmount.inSEK(4500))
                                        .build())
                        .withId(ID_MODULE)
                        .putInTemporaryStorage("box", box)
                        .build()
                        .orElse(null);

        Optional<SomeBoxing> storage = account.getFromTemporaryStorage("box", SomeBoxing.class);

        assertTrue(storage.isPresent());
        assertEquals("TestString", storage.get().x);
        assertEquals(15, storage.get().y);
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
    public void testHolderName() {
        TransactionalAccount account =
                TransactionalAccount.nxBuilder()
                        .withType(TransactionalAccountType.SAVINGS)
                        .withoutFlags()
                        .withBalance(BalanceModule.of(ExactCurrencyAmount.inNOK(3483.23)))
                        .withId(ID_MODULE)
                        .addHolderName("Britte Larsen")
                        .addHolderName("Britte Larsen")
                        .addHolderName("Sigvird Larsen")
                        .build()
                        .orElse(null);

        assertEquals("Britte Larsen", account.getHolderName().toString());
    }

    @Test
    public void testApiIdentifier() {
        TransactionalAccount account =
                TransactionalAccount.nxBuilder()
                        .withType(TransactionalAccountType.SAVINGS)
                        .withoutFlags()
                        .withBalance(BalanceModule.of(ExactCurrencyAmount.inDKK(20)))
                        .withId(ID_MODULE)
                        .setApiIdentifier("2a3ffe-38320c")
                        .build()
                        .orElse(null);

        assertEquals("2a3ffe-38320c", account.getApiIdentifier());
    }

    @Test
    public void unsuccessfulAndSuccessfulMappings() {
        AccountTypeMapper mapper =
                AccountTypeMapper.builder()
                        .put(AccountTypes.CREDIT_CARD, "a", "b")
                        .put(AccountTypes.LOAN, "c")
                        .put(AccountTypes.CHECKING, AccountFlag.PSD2_PAYMENT_ACCOUNT, "zZ")
                        .build();

        Optional<TransactionalAccount> creditCardAccount =
                TransactionalAccount.nxBuilder()
                        .withTypeAndFlagsFrom(mapper, "a")
                        .withBalance(BalanceModule.of(ExactCurrencyAmount.zero("SEK")))
                        .withId(ID_MODULE)
                        .build();

        Optional<TransactionalAccount> loanAccount =
                TransactionalAccount.nxBuilder()
                        .withTypeAndFlagsFrom(mapper, "c")
                        .withBalance(BalanceModule.of(ExactCurrencyAmount.zero("SEK")))
                        .withId(ID_MODULE)
                        .build();

        Optional<TransactionalAccount> checkingAccount =
                TransactionalAccount.nxBuilder()
                        .withTypeAndFlagsFrom(mapper, "zz")
                        .withBalance(BalanceModule.of(ExactCurrencyAmount.zero("SEK")))
                        .withId(ID_MODULE)
                        .build();

        assertEquals(Optional.empty(), creditCardAccount);
        assertEquals(Optional.empty(), loanAccount);
        assertTrue(checkingAccount.isPresent());
        assertTrue(
                checkingAccount.get().getAccountFlags().contains(AccountFlag.PSD2_PAYMENT_ACCOUNT));
    }

    @Test
    public void testSuccessfulBuild() {
        SomeBoxing box = new SomeBoxing("TestString", 15);

        TransactionalAccount account =
                TransactionalAccount.nxBuilder()
                        .withType(TransactionalAccountType.SAVINGS)
                        .withoutFlags()
                        .withBalance(
                                BalanceModule.builder()
                                        .withBalance(ExactCurrencyAmount.inEUR(579.3))
                                        .setAvailableCredit(ExactCurrencyAmount.inEUR(420.7))
                                        .setAvailableBalance(ExactCurrencyAmount.of(529.3, "EUR"))
                                        .setCreditLimit(ExactCurrencyAmount.of(1000.0, "EUR"))
                                        .setInterestRate(0.00155)
                                        .build())
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier("321-573-128")
                                        .withAccountNumber("B-321-573-128")
                                        .withAccountName("Meine Pezparinger")
                                        .addIdentifier(
                                                new SepaEurIdentifier("DE75512108001245126199"))
                                        .addIdentifier(new IbanIdentifier("DE75512108001245126199"))
                                        .setProductName("UltraSavings ZeroFX")
                                        .build())
                        .addHolderName("Jürgen Flughaubtkopf")
                        .addParties(new Party("Millie-Jo Morales", Role.AUTHORIZED_USER))
                        .setHolderType(AccountHolderType.PERSONAL)
                        .setApiIdentifier("2a3ffe-38320c")
                        .putInTemporaryStorage("box", box)
                        .build()
                        .orElse(null);

        Optional<SomeBoxing> storage = account.getFromTemporaryStorage("box", SomeBoxing.class);

        assertThat(account.getType()).isEqualTo(AccountTypes.SAVINGS);
        assertThat(account.getIdModule().getUniqueId()).isEqualTo("321573128");
        assertThat(account.getAccountNumber()).isEqualTo("B-321-573-128");

        assertThat(
                        account.getIdModule().getIdentifiers().stream()
                                .map(AccountIdentifier::toString)
                                .sorted()
                                .collect(Collectors.joining(";")))
                .isEqualTo("iban://DE75512108001245126199;sepa-eur://DE75512108001245126199");
        assertThat(account.getIdModule().getAccountName()).isEqualTo("Meine Pezparinger");
        assertThat(account.getIdModule().getProductName()).isEqualTo("UltraSavings ZeroFX");

        assertThat(account.getExactBalance().getDoubleValue()).isEqualTo(579.3);
        assertThat(account.getExactBalance().getCurrencyCode()).isEqualTo("EUR");
        assertThat(account.getExactAvailableCredit()).isNotNull();
        assertThat(account.getExactAvailableCredit().getDoubleValue()).isEqualTo(420.7);
        assertThat(account.getExactAvailableBalance()).isNotNull();
        assertThat(account.getExactAvailableBalance().getDoubleValue()).isEqualTo(529.3);
        assertThat(account.getExactAvailableBalance().getCurrencyCode()).isEqualTo("EUR");
        assertThat(account.getExactCreditLimit()).isNotNull();
        assertThat(account.getExactCreditLimit().getDoubleValue()).isEqualTo(1000.0);
        assertThat(account.getExactCreditLimit().getCurrencyCode()).isEqualTo("EUR");

        assertThat(storage.isPresent()).isTrue();
        assertThat(storage.get().x).isEqualTo("TestString");
        assertThat(storage.get().y).isEqualTo(15);
        assertThat(account.getHolderName().toString()).isEqualTo("Jürgen Flughaubtkopf");
        assertThat(account.getParties()).hasSize(2);
        assertThat(account.getParties().get(0).getName()).isEqualTo("Jürgen Flughaubtkopf");
        assertThat(account.getParties().get(0).getRole()).isEqualTo(Role.HOLDER);
        assertThat(account.getParties().get(1).getName()).isEqualTo("Millie-Jo Morales");
        assertThat(account.getParties().get(1).getRole()).isEqualTo(Role.AUTHORIZED_USER);
        assertThat(account.getHolderType()).isEqualTo(AccountHolderType.PERSONAL);

        assertThat(account.getApiIdentifier()).isEqualTo("2a3ffe-38320c");
    }
}
