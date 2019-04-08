package se.tink.backend.aggregation.nxgen.core.account.transactional.builder;

import javax.annotation.Nonnull;

public interface AccountNumberStep<S> {

    /**
     * Sets the account number of the account.
     *
     * @param accountNumber The account number
     * @return The next step of the builder
     */
    BalanceStep<S> setAccountNumber(@Nonnull String accountNumber);
}
