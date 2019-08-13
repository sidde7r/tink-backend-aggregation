package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.loan;

import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.builder.BuildStep;

public interface LoanBuildStep extends BuildStep<LoanAccount, LoanBuildStep> {

    /**
     * Constructs an account from this builder.
     *
     * @return An account with the data provided to this builder.
     */
    LoanAccount build();
}
