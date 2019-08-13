package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.investment;

import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.builder.BuildStep;

public interface InvestmentBuildStep extends BuildStep<InvestmentAccount, InvestmentBuildStep> {

    /**
     * Constructs an account from this builder.
     *
     * @return An account with the data provided to this builder.
     */
    InvestmentAccount build();
}
