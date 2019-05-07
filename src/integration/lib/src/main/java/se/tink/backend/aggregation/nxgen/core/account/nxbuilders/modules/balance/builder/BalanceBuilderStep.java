package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder;

import javax.annotation.Nonnull;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.libraries.amount.Amount;

public interface BalanceBuilderStep {

    BalanceBuilderStep setInterestRate(double interestRate);

    BalanceBuilderStep setAvailableCredit(@Nonnull Amount availableCredit);

    BalanceModule build();
}
