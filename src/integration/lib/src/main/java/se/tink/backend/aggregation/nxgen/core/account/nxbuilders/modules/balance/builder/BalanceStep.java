package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder;

import javax.annotation.Nonnull;
import se.tink.libraries.amount.ExactCurrencyAmount;

public interface BalanceStep<T> {

    /**
     * Sets the balance of the account.
     *
     * @param balance The balance to be set
     * @return The next step of the builder
     */
    T withBalance(@Nonnull ExactCurrencyAmount balance);
}
