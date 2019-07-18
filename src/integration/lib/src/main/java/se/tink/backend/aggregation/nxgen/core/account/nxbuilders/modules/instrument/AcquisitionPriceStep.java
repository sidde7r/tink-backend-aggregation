package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument;

public interface AcquisitionPriceStep<T> {
    CurrencyStep<T> withAverageAcquisitionPrice(double averageAcquisitionPrice);
}
