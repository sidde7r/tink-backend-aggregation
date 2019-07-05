package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.builder;

import java.time.LocalDate;
import java.util.List;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.libraries.amount.ExactCurrencyAmount;

public interface LoanModuleBuildStep {

    LoanModuleBuildStep setAmortized(ExactCurrencyAmount amortized);

    LoanModuleBuildStep setMonthlyAmortization(ExactCurrencyAmount monthlyAmortization);

    LoanModuleBuildStep setInitialBalance(ExactCurrencyAmount initialBalance);

    LoanModuleBuildStep setInitialDate(LocalDate initialDate);

    LoanModuleBuildStep setLoanNumber(String loanNumber);

    LoanModuleBuildStep setNumMonthsBound(int numMonthsBound);

    LoanModuleBuildStep setNextDayOfTermsChange(LocalDate nextDayOfTermsChange);

    LoanModuleBuildStep setSecurity(String security);

    LoanModuleBuildStep setApplicants(List<String> applicants);

    LoanModuleBuildStep setCoApplicant(boolean coApplicant);

    LoanModule build();
}
