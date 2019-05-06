package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.builder;

import javax.annotation.Nonnull;

public interface AccountNumberStep<T> {

    /**
     * Sets the account number of the account.
     *
     * @param accountNumber The account number
     * @return The next step of the builder
     */
    AccountNameStep<T> setAccountNumber(@Nonnull String accountNumber);
}
