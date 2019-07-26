package se.tink.backend.aggregation.nxgen.core.account.loan;

import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.nxgen.core.account.AccountBuilder;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.builder.WithIdStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.loan.LoanBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.loan.LoanDetailsStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;

public class LoanAccountBuilder extends AccountBuilder<LoanAccount, LoanBuildStep>
        implements LoanDetailsStep<LoanBuildStep>, LoanBuildStep {

    LoanModule loanModule;

    @Override
    public WithIdStep<LoanBuildStep> withLoanDetails(LoanModule loanDetails) {
        Preconditions.checkNotNull(loanDetails, "Loan Details must not be null.");

        this.loanModule = loanDetails;
        return this;
    }

    @Override
    protected LoanBuildStep buildStep() {
        return this;
    }

    @Override
    public LoanAccount build() {
        return new LoanAccount(this);
    }
}
