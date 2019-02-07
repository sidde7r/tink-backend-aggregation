package se.tink.backend.aggregation.nxgen.core.account.transactional.builder;

import se.tink.backend.aggregation.nxgen.core.account.transactional.SavingsAccount;

import javax.annotation.Nonnull;

public interface SavingsBuildStep extends BuildStep<SavingsAccount, SavingsBuildStep> {

    /**
     * Sets the interest rate of the account.
     * @param interestRate The interest rate
     * @return The final step of the builder
     */
    SavingsBuildStep setInterestRate(@Nonnull Double interestRate);
}
