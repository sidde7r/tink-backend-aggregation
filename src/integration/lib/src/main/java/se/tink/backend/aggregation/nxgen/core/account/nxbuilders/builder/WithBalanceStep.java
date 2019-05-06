package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.builder;

import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;

public interface WithBalanceStep<T> {

    T withBalance(BalanceModule balance);
}
