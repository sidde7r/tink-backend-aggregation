package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.loan;

import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.builder.WithIdStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;

public interface LoanDetailsStep<T> {

    WithIdStep<T> withLoanDetails(LoanModule loanDetails);
}
