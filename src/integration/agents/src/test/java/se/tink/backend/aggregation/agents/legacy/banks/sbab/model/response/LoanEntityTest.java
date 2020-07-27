package se.tink.backend.aggregation.agents.banks.sbab.model.response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;
import org.junit.Test;
import se.tink.backend.aggregation.agents.banks.sbab.entities.LoanEntity;
import se.tink.backend.aggregation.agents.banks.sbab.entities.LoanTermsEntity;
import se.tink.backend.aggregation.agents.models.Loan;

public class LoanEntityTest {

    @Test
    public void emptyTermsChangeDate_ReturnsLoanOptionalPresent() {
        LoanEntity loanEntity = new LoanEntity();
        LoanTermsEntity loanTerms = new LoanTermsEntity();
        loanEntity.setLoanTerms(loanTerms);
        loanEntity.setAmount(BigDecimal.valueOf(200000.0));
        loanEntity.setLoanNumber(BigInteger.valueOf(12345));

        Optional<Loan> loan = loanEntity.toTinkLoan();
        assertTrue(loan.isPresent());
        assertNull(loan.get().getNextDayOfTermsChange());

        loanTerms.setNextDayOfTermsChange(0);

        loan = loanEntity.toTinkLoan();
        assertTrue(loan.isPresent());
        assertNull(loan.get().getNextDayOfTermsChange());
    }

    @Test
    public void mortgageAmount_IsSetCorrectly() {
        LoanEntity loanEntity = new LoanEntity();
        loanEntity.setLoanTerms(new LoanTermsEntity());
        loanEntity.setAmount(BigDecimal.valueOf(877500.0));
        loanEntity.setLoanNumber(BigInteger.valueOf(12345));

        assertEquals(-877500, loanEntity.toTinkAccount().get().getBalance(), 0);
        assertEquals(-877500, loanEntity.toTinkLoan().get().getBalance(), 0);
    }

    @Test
    public void numMonthsBound_IsParsedCorrectly() {
        LoanEntity loanEntity = new LoanEntity();
        LoanTermsEntity loanTerms = new LoanTermsEntity();
        loanEntity.setLoanTerms(loanTerms);
        loanEntity.setAmount(BigDecimal.valueOf(877500.0));
        loanEntity.setLoanNumber(BigInteger.valueOf(12345));

        loanTerms.setInterestRateBoundPeriod("MONTHS_3");
        assertEquals(3, loanEntity.toTinkLoan().get().getNumMonthsBound(), 0);

        loanTerms.setInterestRateBoundPeriod("MONTHS_12");
        assertEquals(12, loanEntity.toTinkLoan().get().getNumMonthsBound(), 0);

        loanTerms.setInterestRateBoundPeriod("YEARS_OR_SOMETHING_ELSE_UNKNOWN_3");
        assertNull(loanEntity.toTinkLoan().get().getNumMonthsBound());
    }

    @Test
    public void amortizationValue_IsSetCorrectly() {
        LoanEntity loanEntity = new LoanEntity();
        LoanTermsEntity loanTerms = new LoanTermsEntity();
        loanEntity.setLoanTerms(loanTerms);
        loanEntity.setAmount(BigDecimal.valueOf(877500.0));
        loanEntity.setLoanNumber(BigInteger.valueOf(12345));

        loanTerms.setAmortizationValue(500.0);
        assertEquals(loanEntity.toTinkLoan().get().getMonthlyAmortization(), 500.0, 0);

        loanTerms.setAmortizationValue(1234.56);
        assertEquals(loanEntity.toTinkLoan().get().getMonthlyAmortization(), 1234.56, 0);
    }

    @Test
    public void nullLoanTerms_ReturnsLoanOptionalAbsent() {
        LoanEntity loanEntity = new LoanEntity();
        loanEntity.setAmount(BigDecimal.valueOf(877500.0));
        loanEntity.setLoanNumber(BigInteger.valueOf(12345));

        assertFalse(loanEntity.toTinkLoan().isPresent());
    }

    @Test
    public void noLoanNumber_ReturnsLoanOptionalAbsent() {
        LoanEntity loanEntity = new LoanEntity();
        loanEntity.setAmount(BigDecimal.valueOf(877500.0));
        loanEntity.setLoanTerms(new LoanTermsEntity());

        assertFalse(loanEntity.toTinkLoan().isPresent());
    }

    @Test
    public void equalInterestRate() {
        Loan loan = new Loan();
        LoanTermsEntity loanTermsEntity = new LoanTermsEntity();
        loanTermsEntity.setInterestRate(0.009);
        loan.setInterest(loanTermsEntity.getNormalizedInterestRate());
        assertEquals(0.009, loan.getInterest(), 0.0000001);
    }
}
