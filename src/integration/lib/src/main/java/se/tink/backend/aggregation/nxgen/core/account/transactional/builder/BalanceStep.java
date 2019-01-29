package se.tink.backend.aggregation.nxgen.core.account.transactional.builder;

import se.tink.libraries.amount.Amount;

import javax.annotation.Nonnull;

public interface BalanceStep<S> {

    /**
     * Sets the balance of the account.
     * @param balance The balance to be set
     * @return The next step of the builder
     */
    AccountIdentifierStep<S> setBalance(@Nonnull Amount balance);
}
