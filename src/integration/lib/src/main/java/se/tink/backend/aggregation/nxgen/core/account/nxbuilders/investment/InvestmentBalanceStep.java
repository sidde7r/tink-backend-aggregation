package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.investment;

import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.builder.WithIdStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;
import se.tink.libraries.amount.ExactCurrencyAmount;

public interface InvestmentBalanceStep<T> {
    /**
     * Using this method indicates that the cash balance of the investment account is zero.
     *
     * <p>Uses the sum of total values of the account portfolios {@link
     * PortfolioModule#getTotalValue()} as account balance.
     *
     * <p>Similar to <code>
     *   withCashBalance(ExactCurrencyAmount.zero(currencyCode));
     * </code>
     */
    WithIdStep<T> withZeroCashBalance(String currencyCode);

    /**
     * Adds the specified cash balance to the sum of total values of the account portfolios {@link
     * PortfolioModule#getTotalValue()} and use it as the account balance.
     *
     * @param cashBalance the cash balance amount which will be added to the calculated sum of the
     *     portfolios
     */
    WithIdStep<T> withCashBalance(ExactCurrencyAmount cashBalance);
}
