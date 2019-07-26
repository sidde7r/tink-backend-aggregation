package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio;

public interface CashValueStep<T> {
    /** @param cashValue The funds, on this portfolio, available for purchase instruments. */
    TotalProfitStep<T> withCashValue(double cashValue);
}
