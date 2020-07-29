package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities;

import com.google.common.annotations.VisibleForTesting;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAmount;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.id.InstrumentIdModule;

@JsonObject
public class CustodyHoldings {

    private Quantity holdingQuantity;
    private HandelsbankenAmount averagePurchasePrice;
    private HandelsbankenAmount marketValue;
    private HandelsbankenAmount marketPrice;
    private HandelsbankenPerformance performance;
    private SecurityIdentifier securityIdentifier;

    public InstrumentModule applyTo(SecurityHoldingIdentifier identifier, String name) {

        String isin = securityIdentifier.getIsinCode();
        String market = securityIdentifier.getMarket();

        return InstrumentModule.builder()
                .withType(identifier.getTinkType())
                .withId(InstrumentIdModule.of(isin, market, name, isin))
                .withMarketPrice(HandelsbankenAmount.asDoubleOrElseNull(marketPrice))
                .withMarketValue(HandelsbankenAmount.asDoubleOrElseNull(marketValue))
                .withAverageAcquisitionPrice(
                        HandelsbankenAmount.asDoubleOrElseNull(averagePurchasePrice))
                .withCurrency(securityIdentifier.getCurrency())
                .withQuantity(holdingQuantity.asDouble().orElse(null))
                .withProfit(
                        Optional.ofNullable(performance)
                                .flatMap(HandelsbankenPerformance::asDouble)
                                .orElse(null))
                .setRawType(identifier.getType())
                .build();
    }

    public boolean hasNoValue() {
        return holdingQuantity == null
                || securityIdentifier == null
                || holdingQuantity.hasNoValue();
    }

    @VisibleForTesting
    void setHoldingQuantity(Quantity holdingQuantity) {
        this.holdingQuantity = holdingQuantity;
    }

    @VisibleForTesting
    void setSecurityIdentifier(SecurityIdentifier securityIdentifier) {
        this.securityIdentifier = securityIdentifier;
    }

    @VisibleForTesting
    public void setMarketValue(HandelsbankenAmount marketValue) {
        this.marketValue = marketValue;
    }

    @VisibleForTesting
    public void setMarketPrice(HandelsbankenAmount marketPrice) {
        this.marketPrice = marketPrice;
    }
}
