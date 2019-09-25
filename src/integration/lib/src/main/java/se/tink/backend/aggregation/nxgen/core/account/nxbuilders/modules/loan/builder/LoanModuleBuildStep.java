package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.builder;

import java.time.LocalDate;
import java.util.List;
import javax.annotation.Nullable;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.libraries.amount.ExactCurrencyAmount;

public interface LoanModuleBuildStep {

    LoanModuleBuildStep setAmortized(@Nullable ExactCurrencyAmount amortized);

    LoanModuleBuildStep setMonthlyAmortization(@Nullable ExactCurrencyAmount monthlyAmortization);

    LoanModuleBuildStep setInitialBalance(@Nullable ExactCurrencyAmount initialBalance);

    LoanModuleBuildStep setInitialDate(@Nullable LocalDate initialDate);

    LoanModuleBuildStep setLoanNumber(@Nullable String loanNumber);

    LoanModuleBuildStep setNumMonthsBound(@Nullable Integer numMonthsBound);

    LoanModuleBuildStep setNextDayOfTermsChange(@Nullable LocalDate nextDayOfTermsChange);

    LoanModuleBuildStep setSecurity(@Nullable String security);

    LoanModuleBuildStep setApplicants(@Nullable List<String> applicants);

    LoanModuleBuildStep setCoApplicant(@Nullable Boolean coApplicant);

    LoanModule build();
}
