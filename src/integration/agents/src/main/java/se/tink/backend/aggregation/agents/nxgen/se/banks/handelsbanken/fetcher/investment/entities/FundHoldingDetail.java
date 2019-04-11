package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.deserializers.DoubleDeserializer;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAmount;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FundHoldingDetail {
    private double holdingUnits;

    @JsonDeserialize(using = DoubleDeserializer.class)
    private Double marketValueFormatted;

    private HandelsbankenAmount totalChange;
    private HandelsbankenAmount averageValueOfCost;
    private HandelsbankenAmount price;

    public boolean hasNoValue() {
        return holdingUnits == 0d;
    }

    public Instrument applyTo(Instrument instrument) {
        instrument.setAverageAcquisitionPrice(
                HandelsbankenAmount.asDoubleOrElseNull(averageValueOfCost));
        instrument.setMarketValue(marketValueFormatted);
        instrument.setPrice(HandelsbankenAmount.asDoubleOrElseNull(price));
        instrument.setProfit(HandelsbankenAmount.asDoubleOrElseNull(totalChange));
        instrument.setQuantity(holdingUnits);
        return instrument;
    }
}
