package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard;

import java.util.Map;
import javax.annotation.Nonnull;
import se.tink.backend.agents.rpc.AccountBalanceType;
import se.tink.libraries.amount.ExactCurrencyAmount;

public interface CardBalanceStep<T> {

    CardCreditStep<T> withBalance(@Nonnull ExactCurrencyAmount balance);

    CardCreditStep<T> withGranularBalance(
            @Nonnull ExactCurrencyAmount balance,
            @Nonnull Map<AccountBalanceType, ExactCurrencyAmount> granularAccountBalances);
}
