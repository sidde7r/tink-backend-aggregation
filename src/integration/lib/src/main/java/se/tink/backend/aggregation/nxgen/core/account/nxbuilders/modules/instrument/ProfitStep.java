package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument;

public interface ProfitStep<T> {
    TickerStep<T> withProfit(double profit);
}
