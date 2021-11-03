package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard;

import java.time.Instant;
import java.util.Map;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.tuple.Pair;
import se.tink.backend.agents.rpc.AccountBalanceType;
import se.tink.libraries.amount.ExactCurrencyAmount;

public interface CardBalanceStep<T> {

    CardCreditStep<T> withBalance(@Nonnull ExactCurrencyAmount balance);

    CardCreditStep<T> withGranularBalances(
            @Nonnull
                    Map<AccountBalanceType, Pair<ExactCurrencyAmount, Instant>>
                            granularAccountBalances);

    CardCreditStep<T> withBalanceAndGranularBalances(
            @Nonnull ExactCurrencyAmount balance,
            @Nonnull
                    Map<AccountBalanceType, Pair<ExactCurrencyAmount, Instant>>
                            granularAccountBalances);
}
