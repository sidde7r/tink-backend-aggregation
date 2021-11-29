package se.tink.backend.aggregation.nxgen.core.to_system;

import com.google.common.base.Strings;
import java.lang.invoke.MethodHandles;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.models.Loan;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.loan.util.LoanInterpreter;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.date.DateUtils;

public final class LoanAccountConverter {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private LoanAccountConverter() {}

    public static Loan toSystemLoan(LoanInterpreter interpreter, LoanAccount sourceAccount) {
        LoanDetails sourceLoanDetails = sourceAccount.getDetails();

        String loanNumber = sourceLoanDetails.getLoanNumber();

        Loan loan = new Loan();
        loan.setBalance(sourceAccount.getExactBalance().getDoubleValue());
        loan.setInterest(sourceAccount.getInterestRate());
        loan.setName(sourceAccount.getName());
        loan.setLoanNumber(
                Strings.isNullOrEmpty(loanNumber) ? sourceAccount.getAccountNumber() : loanNumber);
        loan.setAmortized(calculateAmortizedValue(sourceAccount, sourceLoanDetails));
        loan.setMonthlyAmortization(
                calculateMonthlyAmortizationValue(sourceAccount, sourceLoanDetails));
        loan.setInitialBalance(getInitialBalanceValue(sourceLoanDetails));
        loan.setInitialDate(sourceLoanDetails.getInitialDate());
        loan.setNumMonthsBound(sourceLoanDetails.getNumMonthsBound());
        loan.setNextDayOfTermsChange(sourceLoanDetails.getNextDayOfTermsChange());

        LoanDetails.Type type =
                LoanDetails.Type.DERIVE_FROM_NAME.equals(sourceLoanDetails.getType())
                        ? interpreter.interpretLoanType(loan.getName())
                        : sourceLoanDetails.getType();

        loan.setType(type.toSystemType());

        se.tink.backend.aggregation.agents.models.LoanDetails loanDetails =
                new se.tink.backend.aggregation.agents.models.LoanDetails();
        loanDetails.setLoanSecurity(sourceLoanDetails.getSecurity());
        loanDetails.setCoApplicant(sourceLoanDetails.hasCoApplicant());
        loanDetails.setApplicants(sourceLoanDetails.getApplicants());

        loan.setLoanDetails(loanDetails);

        return loan;
    }

    private static Double calculateMonthlyAmortizationValue(
            LoanAccount account, LoanDetails loanDetails) {

        ExactCurrencyAmount monthlyAmortization = loanDetails.getExactMonthlyAmortization();
        if (monthlyAmortization != null) {
            return monthlyAmortization.getDoubleValue();
        }
        Double amortizedValue = calculateAmortizedValue(account, loanDetails);

        Date initialDate = loanDetails.getInitialDate();
        if (initialDate == null || amortizedValue == null) {
            return null;
        }

        long monthsAmortized = DateUtils.getCalendarMonthsBetween(initialDate, new Date());
        return monthsAmortized != 0 ? amortizedValue / monthsAmortized : 0;
    }

    private static Double calculateAmortizedValue(LoanAccount account, LoanDetails loanDetails) {
        ExactCurrencyAmount amortized = loanDetails.getExactAmortized();
        if (amortized != null) {
            return amortized.getDoubleValue();
        }

        ExactCurrencyAmount initialBalance = loanDetails.getInitialBalance();
        if (initialBalance != null) {
            if (!Objects.equals(
                    initialBalance.getCurrencyCode(),
                    account.getExactBalance().getCurrencyCode())) {
                logger.warn(
                        String.format(
                                "Detected Multiple loan currencies {balance: %s, initialBalance: %s}",
                                account.getExactBalance().getCurrencyCode(),
                                initialBalance.getCurrencyCode()));
            }

            return initialBalance.subtract(account.getExactBalance()).getDoubleValue();
        }

        return null;
    }

    private static Double getInitialBalanceValue(LoanDetails loanDetails) {
        ExactCurrencyAmount initialBalance = loanDetails.getInitialBalance();
        return Optional.ofNullable(initialBalance)
                .map(ExactCurrencyAmount::getDoubleValue)
                .orElse(null);
    }
}
