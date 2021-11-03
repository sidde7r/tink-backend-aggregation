package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder;

import java.time.Instant;
import java.util.Map;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.tuple.Pair;
import se.tink.backend.agents.rpc.AccountBalanceType;
import se.tink.libraries.amount.ExactCurrencyAmount;

public interface BalanceStep<T> {

    /**
     * Sets the balance of the account.
     *
     * @param balance The balance to be set
     * @return The next step of the builder
     */
    T withBalance(@Nonnull ExactCurrencyAmount balance);

    /**
     * Sets granular balances using ISO Balances Type as key and pair of balance amount with time of
     * the balance snapshot as value.
     *
     * @param granularBalances The balance map to be set
     * @return The next step of the builder
     */
    T withGranularBalances(
            @Nonnull Map<AccountBalanceType, Pair<ExactCurrencyAmount, Instant>> granularBalances);

    /**
     * For temporary use only during transitional period
     *
     * <p>from using withBalance to using withGranularBalances method
     */
    T withBalanceAndGranularBalances(
            @Nonnull ExactCurrencyAmount balance,
            @Nonnull Map<AccountBalanceType, Pair<ExactCurrencyAmount, Instant>> granularBalances);
}
