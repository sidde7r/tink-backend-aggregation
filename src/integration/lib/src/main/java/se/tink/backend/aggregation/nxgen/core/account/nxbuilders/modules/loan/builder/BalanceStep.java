package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.builder;

import se.tink.libraries.amount.ExactCurrencyAmount;

public interface BalanceStep<T> {

    InterestStep<T> withBalance(ExactCurrencyAmount amount);
}
