package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.rpc;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities.HandelsbankenSEFundDetails;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAmount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.agents.models.Instrument;

public class HandelsbankenSEFundAccountHoldingDetail extends BaseResponse {

    private String currency;
    private HandelsbankenSEFundDetails fundDetails;
    private String isin;
    private HandelsbankenAmount marketValue;
    private HandelsbankenAmount averageValueOfCost;

    public Optional<Instrument> toInstrument() {
        return Optional.ofNullable(marketValue)
                .map(HandelsbankenAmount::asDouble)
                .filter(marketValue -> marketValue != 0d)
                .map(extractedMarketValue -> {
                    Instrument instrument = new Instrument();
                    instrument.setMarketValue(extractedMarketValue);
                    instrument.setCurrency(this.marketValue.getCurrency());
                    Optional<HandelsbankenSEFundDetails> details = Optional.ofNullable(this.fundDetails);
                    String isin = Optional.ofNullable(this.isin).orElse(
                            details.map(HandelsbankenSEFundDetails::getIsin)
                                    .orElse(null)
                    );
                    String marketPlace = details.map(HandelsbankenSEFundDetails::getExternalFundId).orElse(null);
                    instrument.setIsin(isin);
                    instrument.setMarketPlace(marketPlace);
                    instrument.setUniqueIdentifier(isin + marketPlace);
                    instrument.setName(details.map(HandelsbankenSEFundDetails::getName).orElse(null));
                    instrument.setAverageAcquisitionPrice(
                            HandelsbankenAmount.asDoubleOrElseNull(averageValueOfCost)
                    );
                    instrument.setQuantity(
                            details.flatMap(HandelsbankenSEFundDetails::parseNavAmount)
                                    .filter(navAmount -> navAmount != 0d)
                                    .map(navAmount -> Math.floor(extractedMarketValue / navAmount))
                                    .orElse(null)
                    );
                    instrument.setType(Instrument.Type.FUND);
                    return instrument;
                });
    }
}
