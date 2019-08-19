package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.builder;

import javax.annotation.Nonnull;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;

public interface WithBalanceStep<T> {

    WithIdStep<T> withBalance(@Nonnull BalanceModule balance);
}
