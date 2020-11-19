package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.entities;

import static org.assertj.core.api.Assertions.assertThat;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.LoanDetailsResponse;

@RunWith(JUnitParamsRunner.class)
public class LoanEntityTest {

    private static final String INTEREST = "221.34";
    private static final String CASH_DEBT = "2108000.00";
    private static final String PAYMENT_FREQUENCY = "12";
    private static final Double CALCULATION_RESULT = 0.00126;

    @Test
    public void parseInterestRate() {
        // Given
        LoanDetailEntity loanDetailEntity = new LoanDetailEntity();
        loanDetailEntity.setInterest(INTEREST);
        loanDetailEntity.setCashDebt(CASH_DEBT);
        loanDetailEntity.setPaymentFrequency(PAYMENT_FREQUENCY);

        LoanDetailsResponse loanDetailsResponse = new LoanDetailsResponse();
        loanDetailsResponse.setLoanDetail(loanDetailEntity);

        LoanEntity loanEntity = new LoanEntity();

        // When
        double result = loanEntity.parseInterestRate(loanDetailsResponse);

        // Then
        assertThat(result).isEqualTo(CALCULATION_RESULT);
    }

    @Test
    @Parameters(method = "loanDetailNullParameters")
    public void parseInterestRateWhenOneOfParamsIsNullOrEmpty(
            String interest, String cashDebt, String paymentFrequency) {
        // Given
        LoanDetailEntity loanDetailEntity = new LoanDetailEntity();
        loanDetailEntity.setInterest(interest);
        loanDetailEntity.setCashDebt(cashDebt);
        loanDetailEntity.setPaymentFrequency(paymentFrequency);

        LoanDetailsResponse loanDetailsResponse = new LoanDetailsResponse();
        loanDetailsResponse.setLoanDetail(loanDetailEntity);

        LoanEntity loanEntity = new LoanEntity();

        // When
        Double result = loanEntity.parseInterestRate(loanDetailsResponse);

        // Then
        assertThat(result).isNull();
    }

    private Object[] loanDetailNullParameters() {
        return new Object[] {
            new Object[] {null, CASH_DEBT, PAYMENT_FREQUENCY},
            new Object[] {INTEREST, null, PAYMENT_FREQUENCY},
            new Object[] {INTEREST, CASH_DEBT, null},
            new Object[] {null, null, null},
            new Object[] {"", CASH_DEBT, PAYMENT_FREQUENCY},
            new Object[] {INTEREST, "", PAYMENT_FREQUENCY},
            new Object[] {INTEREST, CASH_DEBT, ""},
            new Object[] {"", "", ""},
            new Object[] {"0", "0", "0"},
        };
    }
}
