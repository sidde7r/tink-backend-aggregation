package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument;

public interface QuantityStep<T> {
    ProfitStep<T> withQuantity(double quantity);
}
