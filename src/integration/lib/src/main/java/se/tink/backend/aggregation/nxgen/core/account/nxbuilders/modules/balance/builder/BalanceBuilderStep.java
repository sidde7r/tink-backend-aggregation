package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder;

import javax.annotation.Nonnull;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.libraries.amount.ExactCurrencyAmount;

public interface BalanceBuilderStep {

    BalanceBuilderStep setInterestRate(double interestRate);

    BalanceBuilderStep setAvailableCredit(@Nonnull ExactCurrencyAmount availableCredit);

    /**
     * @param availableBalance The amount of funds the customer is able to withdraw from the
     *     account, not including any overdraft facility that may be available. Typically this will
     *     be the balance, minus any pending card transactions and minus any uncleared cheques.
     * @return balance builder step
     */
    BalanceBuilderStep setAvailableBalance(@Nonnull ExactCurrencyAmount availableBalance);

    /**
     * @param creditLimit the total amount of any credit facility available on the account
     * @return balance builder step
     */
    BalanceBuilderStep setCreditLimit(@Nonnull ExactCurrencyAmount creditLimit);

    BalanceModule build();
}
