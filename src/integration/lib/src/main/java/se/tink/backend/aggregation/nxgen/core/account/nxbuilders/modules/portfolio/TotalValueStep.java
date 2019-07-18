package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio;

public interface TotalValueStep<T> {
    WithInstrumentStep<T> withTotalValue(double totalValue);
}
