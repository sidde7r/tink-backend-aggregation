package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.entities;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.LoanDetailsResponse;
import se.tink.libraries.amount.ExactCurrencyAmount;

@RunWith(JUnitParamsRunner.class)
public class LoanEntityTest {

    private static final String INTEREST = "221.34";
    private static final String CASH_DEBT = "2108000.00";
    private static final String DEBT_AMOUNT = "2108000.00";
    private static final String PRINCIPAL = "1230000.00";
    private static final String PAYMENT_FREQUENCY = "12";
    private static final Double CALCULATION_RESULT = 0.00126;
    private static final String CURRENCY_CODE = "DKK";
    private static final String ZERO = "0";
    private static final String REMAINING_YEARS_OR_MONTHS = "10";

    @Test
    @Parameters(method = "parametersOfLoanDetail")
    public void parseInterestRate(
            String interest, String cashDebt, String paymentFrequency, String debtAmount) {
        // Given
        LoanEntity loanEntity = new LoanEntity();

        // When
        double result =
                loanEntity.parseInterestRate(
                        getLoanDetailsResponseToCalculateInterestRate(
                                interest, cashDebt, paymentFrequency, debtAmount));

        // Then
        assertThat(result).isEqualTo(CALCULATION_RESULT);
    }

    private LoanDetailsResponse getLoanDetailsResponseToCalculateInterestRate(
            String interest, String cashDebt, String paymentFrequency, String debtAmount) {
        LoanDetailEntity loanDetailEntity = new LoanDetailEntity();
        loanDetailEntity.setInterest(interest);
        loanDetailEntity.setCashDebt(cashDebt);
        loanDetailEntity.setPaymentFrequency(paymentFrequency);
        loanDetailEntity.setDebtAmount(debtAmount);

        LoanDetailsResponse loanDetailsResponse = new LoanDetailsResponse();
        loanDetailsResponse.setLoanDetail(loanDetailEntity);
        return loanDetailsResponse;
    }

    private Object[] parametersOfLoanDetail() {
        return new Object[] {
            new Object[] {INTEREST, CASH_DEBT, PAYMENT_FREQUENCY, DEBT_AMOUNT},
            new Object[] {INTEREST, null, PAYMENT_FREQUENCY, DEBT_AMOUNT},
            new Object[] {INTEREST, "", PAYMENT_FREQUENCY, DEBT_AMOUNT},
            new Object[] {INTEREST, CASH_DEBT, PAYMENT_FREQUENCY, null},
            new Object[] {INTEREST, CASH_DEBT, PAYMENT_FREQUENCY, ""},
        };
    }

    @Test
    @Parameters(method = "loanDetailNullParameters")
    public void parseInterestRateWhenOneOfParamsIsNullOrEmpty(
            String interest, String cashDebt, String paymentFrequency, String debtAmount) {
        // Given
        LoanEntity loanEntity = new LoanEntity();

        // When
        Double result =
                loanEntity.parseInterestRate(
                        getLoanDetailsResponseToCalculateInterestRate(
                                interest, cashDebt, paymentFrequency, debtAmount));

        // Then
        assertThat(result).isNull();
    }

    private Object[] loanDetailNullParameters() {
        return new Object[] {
            new Object[] {null, CASH_DEBT, PAYMENT_FREQUENCY, DEBT_AMOUNT},
            new Object[] {INTEREST, null, PAYMENT_FREQUENCY, null},
            new Object[] {INTEREST, CASH_DEBT, null, DEBT_AMOUNT},
            new Object[] {null, null, null, null},
            new Object[] {"", CASH_DEBT, PAYMENT_FREQUENCY, DEBT_AMOUNT},
            new Object[] {INTEREST, "", PAYMENT_FREQUENCY, ""},
            new Object[] {INTEREST, CASH_DEBT, "", DEBT_AMOUNT},
            new Object[] {"", "", "", ""},
            new Object[] {ZERO, ZERO, ZERO, ZERO},
        };
    }

    @Test
    @Parameters(method = "balanceParameters")
    public void shouldGetBalance(int outstandingDebt, String expected) {
        // given
        LoanEntity loanEntity = new LoanEntity();
        loanEntity.setOutstandingDebt(outstandingDebt);
        loanEntity.setCurrencyCode(CURRENCY_CODE);

        // when
        ExactCurrencyAmount result = loanEntity.getBalance();

        // then
        assertThat(result)
                .isEqualTo(ExactCurrencyAmount.of(new BigDecimal(expected), CURRENCY_CODE));
    }

    private Object[] balanceParameters() {
        return new Object[] {
            new Object[] {1234567, "-1234567"},
            new Object[] {0, ZERO},
        };
    }

    @Test
    @Parameters(method = "numberOfMonthsBoundParameters")
    public void shouldCalculateNumberOfMonthsBound(
            String remainingLoanPeriodYearly, String remainingLoanPeriodMonthly, Integer expected) {
        // given
        LoanDetailEntity loanDetailEntity = new LoanDetailEntity();
        loanDetailEntity.setRemainingLoanPeriodYearly(remainingLoanPeriodYearly);
        loanDetailEntity.setRemainingLoanPeriodMonthly(remainingLoanPeriodMonthly);

        LoanDetailsResponse loanDetailsResponse = new LoanDetailsResponse();
        loanDetailsResponse.setLoanDetail(loanDetailEntity);

        // when
        Integer result = loanDetailsResponse.calculateNumberOfMonthsBound();

        // then
        assertThat(result).isEqualTo(expected);
    }

    private Object[] numberOfMonthsBoundParameters() {
        return new Object[] {
            new Object[] {REMAINING_YEARS_OR_MONTHS, REMAINING_YEARS_OR_MONTHS, 130},
            new Object[] {"", REMAINING_YEARS_OR_MONTHS, 10},
            new Object[] {null, REMAINING_YEARS_OR_MONTHS, 10},
            new Object[] {REMAINING_YEARS_OR_MONTHS, "", 120},
            new Object[] {REMAINING_YEARS_OR_MONTHS, null, 120},
            new Object[] {"", "", null},
            new Object[] {null, null, null},
        };
    }

    @Test
    @Parameters(method = "principalParameters")
    public void shouldGetPrincipal(
            String principal, String currency, ExactCurrencyAmount expected) {
        // given
        LoanDetailEntity loanDetailEntity = new LoanDetailEntity();
        loanDetailEntity.setPrincipal(principal);

        LoanDetailsResponse loanDetailsResponse = new LoanDetailsResponse();
        loanDetailsResponse.setLoanDetail(loanDetailEntity);

        // when
        ExactCurrencyAmount result = loanDetailsResponse.getPrincipal(currency);

        // then
        assertThat(result).isEqualTo(expected);
    }

    private Object[] principalParameters() {
        return new Object[] {
            new Object[] {
                PRINCIPAL, CURRENCY_CODE, ExactCurrencyAmount.of(PRINCIPAL, CURRENCY_CODE)
            },
            new Object[] {"", CURRENCY_CODE, null},
            new Object[] {null, CURRENCY_CODE, null},
            new Object[] {null, "", null},
            new Object[] {null, null, null},
            new Object[] {PRINCIPAL, "", ExactCurrencyAmount.of(PRINCIPAL, "")},
            new Object[] {PRINCIPAL, null, ExactCurrencyAmount.of(PRINCIPAL, null)},
            new Object[] {ZERO, null, ExactCurrencyAmount.of(ZERO, null)},
        };
    }
}
