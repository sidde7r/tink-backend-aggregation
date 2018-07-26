package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities;

import com.google.common.annotations.VisibleForTesting;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAmount;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.system.rpc.Instrument;

@JsonObject
public class CustodyHoldings implements SecurityHoldingContainer.InstrumentEnricher {

    private Quantity holdingQuantity;
    private HandelsbankenAmount averagePurchasePrice;
    private HandelsbankenAmount marketValue;
    private HandelsbankenAmount marketPrice;
    private HandelsbankenPerformance performance;

    @Override
    public Instrument applyTo(Instrument instrument) {
        if (holdingQuantity == null ) {
            return instrument;
        }
        instrument.setQuantity(holdingQuantity.asDouble().orElse(null));
        instrument.setAverageAcquisitionPrice(HandelsbankenAmount.asDoubleOrElseNull(averagePurchasePrice));
        instrument.setMarketValue(HandelsbankenAmount.asDoubleOrElseNull(marketValue));
        instrument.setPrice(HandelsbankenAmount.asDoubleOrElseNull(marketPrice));
        instrument.setProfit(Optional.ofNullable(performance)
                .flatMap(HandelsbankenPerformance::asDouble)
                .orElse(null)
        );
        return instrument;
    }

    public boolean hasNoValue() {
        return holdingQuantity == null || holdingQuantity.hasNoValue();
    }

    @VisibleForTesting
    void setHoldingQuantity(Quantity holdingQuantity) {
        this.holdingQuantity = holdingQuantity;
    }
}
