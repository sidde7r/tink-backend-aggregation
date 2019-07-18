package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument;

public interface MarketPriceStep<T> {
    MarketValueStep<T> withMarketPrice(double marketPrice);
}
