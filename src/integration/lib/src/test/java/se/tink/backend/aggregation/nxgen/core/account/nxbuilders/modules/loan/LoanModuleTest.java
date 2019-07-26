package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import java.time.LocalDate;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class LoanModuleTest {

    @Test(expected = NullPointerException.class)
    public void nullArguments() {
        LoanModule.builder().withType(null).withBalance(null).withInterestRate(0).build();
    }

    @Test(expected = NullPointerException.class)
    public void nullType() {
        LoanModule.builder()
                .withType(null)
                .withBalance(ExactCurrencyAmount.of(2500, "EUR"))
                .withInterestRate(0.1)
                .build();
    }

    @Test(expected = NullPointerException.class)
    public void nullBalance() {
        LoanModule.builder().withType(Type.MORTGAGE).withBalance(null).withInterestRate(0).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeInterest() {
        LoanModule.builder()
                .withType(Type.MORTGAGE)
                .withBalance(ExactCurrencyAmount.of(2500, "EUR"))
                .withInterestRate(-1.24398)
                .build();
    }

    @Test(expected = NullPointerException.class)
    public void nullAmortized() {
        LoanModule.builder()
                .withType(Type.MORTGAGE)
                .withBalance(ExactCurrencyAmount.of(1_560_000.23, "SEK"))
                .withInterestRate(0.01976)
                .setAmortized(null)
                .build();
    }

    @Test(expected = NullPointerException.class)
    public void nullMonthlyAmortization() {
        LoanModule.builder()
                .withType(Type.MORTGAGE)
                .withBalance(ExactCurrencyAmount.of(1_560_000.23, "SEK"))
                .withInterestRate(0.01976)
                .setMonthlyAmortization(null)
                .build();
    }

    @Test(expected = NullPointerException.class)
    public void nullInitialBalance() {
        LoanModule.builder()
                .withType(Type.MORTGAGE)
                .withBalance(ExactCurrencyAmount.of(1_560_000.23, "SEK"))
                .withInterestRate(0.01976)
                .setInitialBalance(null)
                .build();
    }

    @Test(expected = NullPointerException.class)
    public void nullInitialDate() {
        LoanModule.builder()
                .withType(Type.MORTGAGE)
                .withBalance(ExactCurrencyAmount.of(1_560_000.23, "SEK"))
                .withInterestRate(0.01976)
                .setInitialDate(null)
                .build();
    }

    @Test(expected = NullPointerException.class)
    public void nullLoanNumber() {
        LoanModule.builder()
                .withType(Type.MORTGAGE)
                .withBalance(ExactCurrencyAmount.of(1_560_000.23, "SEK"))
                .withInterestRate(0.01976)
                .setLoanNumber(null)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeMonthsBound() {
        LoanModule.builder()
                .withType(Type.MORTGAGE)
                .withBalance(ExactCurrencyAmount.of(1_560_000.23, "SEK"))
                .withInterestRate(0.01976)
                .setNumMonthsBound(-2)
                .build();
    }

    @Test(expected = NullPointerException.class)
    public void nullNextDayOfTermsChange() {
        LoanModule.builder()
                .withType(Type.MORTGAGE)
                .withBalance(ExactCurrencyAmount.of(1_560_000.23, "SEK"))
                .withInterestRate(0.01976)
                .setNextDayOfTermsChange(null)
                .build();
    }

    @Test(expected = NullPointerException.class)
    public void nullSecurity() {
        LoanModule.builder()
                .withType(Type.MORTGAGE)
                .withBalance(ExactCurrencyAmount.of(1_560_000.23, "SEK"))
                .withInterestRate(0.01976)
                .setSecurity(null)
                .build();
    }

    @Test(expected = NullPointerException.class)
    public void nullApplicants() {
        LoanModule.builder()
                .withType(Type.MORTGAGE)
                .withBalance(ExactCurrencyAmount.of(1_560_000.23, "SEK"))
                .withInterestRate(0.01976)
                .setApplicants(null)
                .build();
    }

    @Test
    public void workingBuild() {

        LoanModule loanModule =
                LoanModule.builder()
                        .withType(Type.BLANCO)
                        .withBalance(ExactCurrencyAmount.of(359_641.23, "SEK"))
                        .withInterestRate(15.65)
                        .setAmortized(ExactCurrencyAmount.of(90_358.77, "SEK"))
                        .setApplicants(Lists.newArrayList("Göran Persson", "Mona Sahlin"))
                        .setCoApplicant(true)
                        .setInitialBalance(ExactCurrencyAmount.of(450_000, "SEK"))
                        .setInitialDate(LocalDate.of(2017, 3, 26))
                        .setLoanNumber("XL47282")
                        .setMonthlyAmortization(ExactCurrencyAmount.of(5_000.00, "SEK"))
                        .setNextDayOfTermsChange(LocalDate.of(2022, 5, 1))
                        .setNumMonthsBound(3)
                        .setSecurity("Riksdagshuset")
                        .build();

        assertEquals(Type.BLANCO, loanModule.getLoanType());
        assertEquals(loanModule.getBalance(), ExactCurrencyAmount.of(359_641.23, "SEK"));
        assertEquals(loanModule.getInterestRate(), 15.65, 0);
        assertEquals(loanModule.getAmortized(), ExactCurrencyAmount.of(90_358.77, "SEK"));
        assertEquals(
                loanModule.getApplicants(), Lists.newArrayList("Göran Persson", "Mona Sahlin"));
        assertTrue(loanModule.isCoApplicant());
        assertEquals(loanModule.getInitialBalance(), ExactCurrencyAmount.of(450_000, "SEK"));
        assertEquals(loanModule.getInitialDate(), LocalDate.of(2017, 3, 26));
        assertEquals(loanModule.getLoanNumber(), "XL47282");
        assertEquals(loanModule.getMonthlyAmortization(), ExactCurrencyAmount.of(5_000.00, "SEK"));
        assertEquals(loanModule.getNextDayOfTermsChange(), LocalDate.of(2022, 5, 1));
        assertEquals(loanModule.getNumMonthsBound(), 3);
        assertEquals(loanModule.getSecurity(), "Riksdagshuset");
    }
}
