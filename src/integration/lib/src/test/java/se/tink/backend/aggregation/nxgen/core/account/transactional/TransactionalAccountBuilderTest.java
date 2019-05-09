package se.tink.backend.aggregation.nxgen.core.account.transactional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.AccountBuilder;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.SepaEurIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.Amount;

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

    @Test(expected = IllegalArgumentException.class)
    public void illegalAccountType() {
        // Build an otherwise correct account
        TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.fromAccountType(AccountTypes.LOAN))
                .withId(ID_MODULE)
                .withBalance(BalanceModule.of(Amount.inEUR(2)))
                .build();
    }

    @Test(expected = NullPointerException.class)
    public void nullArguments() {
        TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withId(null)
                .withBalance(null)
                .build();
    }

    @Test(expected = NullPointerException.class)
    public void nullBalance() {
        TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withId(ID_MODULE)
                .withBalance(null)
                .build();
    }

    @Test(expected = NullPointerException.class)
    public void nullId() {
        TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withId(null)
                .withBalance(BalanceModule.of(Amount.inEUR(2)))
                .build();
    }

    @Test(expected = NullPointerException.class)
    public void nullFlagList() {
        TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.SAVINGS)
                .withId(ID_MODULE)
                .withBalance(BalanceModule.of(Amount.inSEK(2572.28)))
                .addAccountFlags((AccountFlag) null)
                .build();
    }

    @Test
    public void testFlagList() {
        TransactionalAccount account =
                TransactionalAccount.nxBuilder()
                        .withType(TransactionalAccountType.SAVINGS)
                        .withId(ID_MODULE)
                        .withBalance(BalanceModule.of(Amount.inSEK(2572.28)))
                        .addAccountFlags(AccountFlag.BUSINESS, AccountFlag.BUSINESS)
                        .addAccountFlags(AccountFlag.MANDATE)
                        .build();

        Assert.assertArrayEquals(
                new AccountFlag[] {AccountFlag.BUSINESS, AccountFlag.MANDATE},
                account.getAccountFlags().toArray());
    }

    @Test
    public void testStorage() {
        SomeBoxing box = new SomeBoxing("TestString", 15);

        TransactionalAccount account =
                TransactionalAccount.nxBuilder()
                        .withType(TransactionalAccountType.OTHER)
                        .withId(ID_MODULE)
                        .withBalance(
                                BalanceModule.builder()
                                        .withBalance(Amount.inSEK(398.32))
                                        .setInterestRate(0.0015)
                                        .setAvailableCredit(Amount.inSEK(4500))
                                        .build())
                        .putInTemporaryStorage("box", box)
                        .build();

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
                        .withId(ID_MODULE)
                        .withBalance(BalanceModule.of(Amount.inNOK(3483.23)))
                        .addHolderName("Britte Larsen")
                        .addHolderName("Britte Larsen")
                        .addHolderName("Sigvird Larsen")
                        .build();

        assertEquals("Britte Larsen", account.getHolderName().toString());
    }

    @Test
    public void testApiIdentifier() {
        TransactionalAccount account =
                TransactionalAccount.nxBuilder()
                        .withType(TransactionalAccountType.SAVINGS)
                        .withId(ID_MODULE)
                        .withBalance(BalanceModule.of(Amount.inDKK(20)))
                        .setApiIdentifier("2a3ffe-38320c")
                        .build();

        assertEquals("2a3ffe-38320c", account.getApiIdentifier());
    }

    @Test
    public void testSuccessfulBuild() {
        SomeBoxing box = new SomeBoxing("TestString", 15);

        TransactionalAccount account =
                TransactionalAccount.nxBuilder()
                        .withType(TransactionalAccountType.SAVINGS)
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
                        .withBalance(
                                BalanceModule.builder()
                                        .withBalance(Amount.inEUR(579.3))
                                        .setAvailableCredit(Amount.inEUR(420.7))
                                        .setInterestRate(0.00155)
                                        .build())
                        .addHolderName("Jürgen Flughaubtkopf")
                        .setApiIdentifier("2a3ffe-38320c")
                        .putInTemporaryStorage("box", box)
                        .build();

        Optional<SomeBoxing> storage = account.getFromTemporaryStorage("box", SomeBoxing.class);

        assertEquals(AccountTypes.SAVINGS, account.getType());
        assertEquals("321573128", account.getIdModule().getUniqueId());
        assertEquals("B-321-573-128", account.getAccountNumber());

        assertEquals(
                "iban://DE75512108001245126199;sepa-eur://DE75512108001245126199",
                account.getIdModule().getIdentifiers().stream()
                        .map(AccountIdentifier::toString)
                        .sorted()
                        .collect(Collectors.joining(";")));
        assertEquals("Meine Pezparinger", account.getIdModule().getAccountName());
        assertEquals("UltraSavings ZeroFX", account.getIdModule().getProductName());

        assertEquals(579.3, account.getBalance().getValue(), 0);
        assertEquals("EUR", account.getBalance().getCurrency());
        assertTrue(account.getBalanceModule().getAvailableCredit().isPresent());
        assertEquals(420.7, account.getBalanceModule().getAvailableCredit().get().getValue(), 0);
        assertTrue(account.getBalanceModule().getInterestRate().isPresent());
        assertEquals(0.00155, account.getBalanceModule().getInterestRate().get(), 0);

        assertTrue(storage.isPresent());
        assertEquals("TestString", storage.get().x);
        assertEquals(15, storage.get().y);
        assertEquals("Jürgen Flughaubtkopf", account.getHolderName().toString());
        assertEquals("2a3ffe-38320c", account.getApiIdentifier());
    }
}
