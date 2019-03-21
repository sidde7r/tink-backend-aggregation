package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities;

import com.google.common.annotations.VisibleForTesting;
import java.util.Optional;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAmount;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CustodyHoldings {

    private Quantity holdingQuantity;
    private HandelsbankenAmount averagePurchasePrice;
    private HandelsbankenAmount marketValue;
    private HandelsbankenAmount marketPrice;
    private HandelsbankenPerformance performance;
    private SecurityIdentifier securityIdentifier;

    public Instrument applyTo(Instrument instrument) {
        if (holdingQuantity == null || securityIdentifier == null) {
            return instrument;
        }

        String isin = securityIdentifier.getIsinCode();
        String market = securityIdentifier.getMarket();

        instrument.setIsin(isin);
        instrument.setMarketPlace(market);
        instrument.setUniqueIdentifier(isin + market);
        instrument.setCurrency(securityIdentifier.getCurrency());
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
        return holdingQuantity == null ||
                securityIdentifier == null ||
                holdingQuantity.hasNoValue();
    }

    @VisibleForTesting
    void setHoldingQuantity(Quantity holdingQuantity) {
        this.holdingQuantity = holdingQuantity;
    }

    @VisibleForTesting
    void setSecurityIdentifier(
            SecurityIdentifier securityIdentifier) {
        this.securityIdentifier = securityIdentifier;
    }
}
