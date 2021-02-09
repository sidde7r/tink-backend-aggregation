package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails.Type;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.builder.BalanceStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.builder.LoanModuleBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.builder.TypeStep;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class LoanModuleTest {

    @Test
    public void nullType() {
        // given
        TypeStep<LoanModuleBuildStep> builder = LoanModule.builder();

        // when
        Throwable t = catchThrowable(() -> builder.withType(null));

        // then
        assertThat(t).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void nullBalance() {
        // given
        BalanceStep<LoanModuleBuildStep> builder = LoanModule.builder().withType(Type.MORTGAGE);

        // when
        Throwable t = catchThrowable(() -> builder.withBalance(null));

        // then
        assertThat(t).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void negativeInterestRate() {
        LoanModule.builder()
                .withType(Type.MORTGAGE)
                .withBalance(ExactCurrencyAmount.of(BigDecimal.valueOf(2500), "#@!$^#$^"))
                .withInterestRate(-1.24398)
                .build();
    }

    @Test
    public void nullAmortized() {
        LoanModule.builder()
                .withType(Type.MORTGAGE)
                .withBalance(ExactCurrencyAmount.of(BigDecimal.valueOf(1_560_000.23), "SEK"))
                .withInterestRate(0.01976)
                .setAmortized(null)
                .build();
    }

    @Test
    public void nullMonthlyAmortization() {
        LoanModule.builder()
                .withType(Type.MORTGAGE)
                .withBalance(ExactCurrencyAmount.of(BigDecimal.valueOf(1_560_000.23), "SEK"))
                .withInterestRate(0.01976)
                .setMonthlyAmortization(null)
                .build();
    }

    @Test
    public void nullInitialBalance() {
        LoanModule.builder()
                .withType(Type.MORTGAGE)
                .withBalance(ExactCurrencyAmount.of(BigDecimal.valueOf(1_560_000.23), "SEK"))
                .withInterestRate(0.01976)
                .setInitialBalance(null)
                .build();
    }

    @Test
    public void nullInitialDate() {
        LoanModule.builder()
                .withType(Type.MORTGAGE)
                .withBalance(ExactCurrencyAmount.of(BigDecimal.valueOf(1_560_000.23), "SEK"))
                .withInterestRate(0.01976)
                .setInitialDate(null)
                .build();
    }

    @Test
    public void nullLoanNumber() {
        LoanModule.builder()
                .withType(Type.MORTGAGE)
                .withBalance(ExactCurrencyAmount.of(BigDecimal.valueOf(1_560_000.23), "SEK"))
                .withInterestRate(0.01976)
                .setLoanNumber(null)
                .build();
    }

    @Test
    public void negativeMonthsBound() {
        // given
        LoanModuleBuildStep builder =
                LoanModule.builder()
                        .withType(Type.MORTGAGE)
                        .withBalance(
                                ExactCurrencyAmount.of(BigDecimal.valueOf(1_560_000.23), "SEK"))
                        .withInterestRate(0.01976);

        // when
        Throwable t = catchThrowable(() -> builder.setNumMonthsBound(-2));

        // then
        assertThat(t).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void nullNextDayOfTermsChange() {
        LoanModule.builder()
                .withType(Type.MORTGAGE)
                .withBalance(ExactCurrencyAmount.of(BigDecimal.valueOf(1_560_000.23), "SEK"))
                .withInterestRate(0.01976)
                .setNextDayOfTermsChange(null)
                .build();
    }

    @Test
    public void nullSecurity() {
        LoanModule.builder()
                .withType(Type.MORTGAGE)
                .withBalance(ExactCurrencyAmount.of(BigDecimal.valueOf(1_560_000.23), "SEK"))
                .withInterestRate(0.01976)
                .setSecurity(null)
                .build();
    }

    @Test
    public void nullApplicants() {
        LoanModule.builder()
                .withType(Type.MORTGAGE)
                .withBalance(ExactCurrencyAmount.of(BigDecimal.valueOf(1_560_000.23), "SEK"))
                .withInterestRate(0.01976)
                .setApplicants(null)
                .build();
    }

    @Test
    public void workingBuild() {

        LoanModule loanModule =
                LoanModule.builder()
                        .withType(Type.BLANCO)
                        .withBalance(ExactCurrencyAmount.of(BigDecimal.valueOf(359_641.23), "SEK"))
                        .withInterestRate(15.65)
                        .setAmortized(ExactCurrencyAmount.of(BigDecimal.valueOf(90_358.77), "SEK"))
                        .setApplicants(Lists.newArrayList("Göran Persson", "Mona Sahlin"))
                        .setCoApplicant(true)
                        .setInitialBalance(
                                ExactCurrencyAmount.of(BigDecimal.valueOf(450_000), "SEK"))
                        .setInitialDate(LocalDate.of(2017, 3, 26))
                        .setLoanNumber("XL47282")
                        .setMonthlyAmortization(
                                ExactCurrencyAmount.of(BigDecimal.valueOf(5_000.00), "SEK"))
                        .setNextDayOfTermsChange(LocalDate.of(2022, 5, 1))
                        .setNumMonthsBound(3)
                        .setSecurity("Riksdagshuset")
                        .build();

        assertEquals(Type.BLANCO, loanModule.getLoanType());
        assertEquals(
                loanModule.getBalance(),
                ExactCurrencyAmount.of(BigDecimal.valueOf(359_641.23), "SEK"));
        assertEquals(loanModule.getInterestRate(), 15.65, 0);
        assertEquals(
                loanModule.getAmortized(),
                ExactCurrencyAmount.of(BigDecimal.valueOf(90_358.77), "SEK"));
        assertEquals(
                loanModule.getApplicants(), Lists.newArrayList("Göran Persson", "Mona Sahlin"));
        assertTrue(loanModule.isCoApplicant());
        assertEquals(
                loanModule.getInitialBalance(),
                ExactCurrencyAmount.of(BigDecimal.valueOf(450_000), "SEK"));
        assertEquals(loanModule.getInitialDate(), LocalDate.of(2017, 3, 26));
        assertEquals(loanModule.getLoanNumber(), "XL47282");
        assertEquals(
                loanModule.getMonthlyAmortization(),
                ExactCurrencyAmount.of(BigDecimal.valueOf(5_000.00), "SEK"));
        assertEquals(loanModule.getNextDayOfTermsChange(), LocalDate.of(2022, 5, 1));
        assertEquals(loanModule.getNumMonthsBound(), Integer.valueOf(3));
        assertEquals(loanModule.getSecurity(), "Riksdagshuset");
    }
}
