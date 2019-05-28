package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder;

import javax.annotation.Nonnull;
import se.tink.backend.agents.rpc.ExactCurrencyAmount;
import se.tink.libraries.amount.Amount;

public interface BalanceStep<T> {

    /**
     * Sets the balance of the account.
     *
     * @param balance The balance to be set
     * @return The next step of the builder
     */
    @Deprecated
    T withBalance(@Nonnull Amount balance);
    /**
     * Sets the balance of the account.
     *
     * @param balance The balance to be set
     * @return The next step of the builder
     */
    T withBalance(@Nonnull ExactCurrencyAmount balance);
}
