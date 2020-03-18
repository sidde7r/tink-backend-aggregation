package se.tink.backend.aggregation.nxgen.core.account.loan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails.Type;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.SepaEurIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class LoanAccountBuilderTest {

    private final IdModule ID_MODULE =
            IdModule.builder()
                    .withUniqueIdentifier("1234")
                    .withAccountNumber("1234")
                    .withAccountName("Account")
                    .addIdentifier(new SwedishIdentifier("123456789"))
                    .build();

    private final LoanModule LOAN_MODULE =
            LoanModule.builder()
                    .withType(Type.MORTGAGE)
                    .withBalance(ExactCurrencyAmount.of(BigDecimal.valueOf(4_372_982.11), "SEK"))
                    .withInterestRate(0.017689)
                    .setLoanNumber("L0000462381243")
                    .setSecurity("2837 Toink Str, 78422 VT")
                    .setInitialDate(LocalDate.of(2012, 11, 4))
                    .build();

    @Test(expected = NullPointerException.class)
    public void nullArguments() {
        LoanAccount.nxBuilder().withLoanDetails(null).withId(null).build();
    }

    @Test(expected = NullPointerException.class)
    public void nullId() {
        LoanAccount.nxBuilder().withLoanDetails(LOAN_MODULE).withId(null).build();
    }

    @Test(expected = NullPointerException.class)
    public void nullFlagList() {
        LoanAccount.nxBuilder()
                .withLoanDetails(LOAN_MODULE)
                .withId(ID_MODULE)
                .addAccountFlags((AccountFlag) null)
                .build();
    }

    @Test
    public void testFlagList() {
        LoanAccount account =
                LoanAccount.nxBuilder()
                        .withLoanDetails(LOAN_MODULE)
                        .withId(ID_MODULE)
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

        LoanAccount account =
                LoanAccount.nxBuilder()
                        .withLoanDetails(LOAN_MODULE)
                        .withId(ID_MODULE)
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
        LoanAccount account =
                LoanAccount.nxBuilder()
                        .withLoanDetails(LOAN_MODULE)
                        .withId(ID_MODULE)
                        .addHolderName("Britte Larsen")
                        .addHolderName("Britte Larsen")
                        .addHolderName("Sigvird Larsen")
                        .build();

        assertEquals("Britte Larsen", account.getHolderName().toString());
    }

    @Test
    public void testApiIdentifier() {
        LoanAccount account =
                LoanAccount.nxBuilder()
                        .withLoanDetails(LOAN_MODULE)
                        .withId(ID_MODULE)
                        .setApiIdentifier("2a3ffe-38320c")
                        .build();

        assertEquals("2a3ffe-38320c", account.getApiIdentifier());
    }

    @Test
    public void testSuccessfulBuild() {
        SomeBoxing box = new SomeBoxing("TestString", 15);

        LoanAccount account =
                LoanAccount.nxBuilder()
                        .withLoanDetails(
                                LoanModule.builder()
                                        .withType(Type.MORTGAGE)
                                        .withBalance(
                                                ExactCurrencyAmount.of(
                                                        BigDecimal.valueOf(2_565_389.43), "SEK"))
                                        .withInterestRate(0.00155)
                                        .setNumMonthsBound(12)
                                        .setCoApplicant(true)
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
                        .setApiIdentifier("2a3ffe-38320c")
                        .setBankIdentifier("1000-2222")
                        .putInTemporaryStorage("box", box)
                        .build();

        Optional<SomeBoxing> storage = account.getFromTemporaryStorage("box", SomeBoxing.class);

        assertThat(account.getType()).isEqualTo(AccountTypes.LOAN);
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

        assertThat(account.getExactBalance().getDoubleValue()).isEqualTo(2_565_389.43);
        assertThat(account.getExactBalance().getCurrencyCode()).isEqualTo("SEK");
        assertThat(account.getExactAvailableCredit()).isNull();
        assertThat(account.getInterestRate()).isEqualTo(0.00155);
        assertThat(account.getDetails().getNumMonthsBound()).isEqualTo(12);

        assertThat(storage.isPresent()).isTrue();
        assertThat(storage.get().x).isEqualTo("TestString");
        assertThat(storage.get().y).isEqualTo(15);
        assertThat(account.getHolderName().toString()).isEqualTo("Jürgen Flughaubtkopf");
        assertThat(account.getApiIdentifier()).isEqualTo("2a3ffe-38320c");
    }
}
