package se.tink.backend.aggregation.nxgen.core.account.transactional.builder;

import javax.annotation.Nonnull;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Deprecated
public interface BalanceStep<S> {

    /**
     * Sets the balance of the account.
     *
     * @param balance The balance to be set
     * @return The next step of the builder
     */
    @Deprecated
    AliasStep<S> setBalance(@Nonnull ExactCurrencyAmount balance);
}
