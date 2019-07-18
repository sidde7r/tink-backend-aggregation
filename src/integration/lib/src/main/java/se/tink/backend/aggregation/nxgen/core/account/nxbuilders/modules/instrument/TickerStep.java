package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument;

public interface TickerStep<T> {
    T withTicker(String ticker);
}
