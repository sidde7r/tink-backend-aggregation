package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio;

public interface TotalProfitStep<T> {
    /**
     * @param totalProfit The total profit of the entire portfolio, both historical (real) profit
     *     and current (potential) profit.
     */
    TotalValueStep<T> withTotalProfit(double totalProfit);
}
