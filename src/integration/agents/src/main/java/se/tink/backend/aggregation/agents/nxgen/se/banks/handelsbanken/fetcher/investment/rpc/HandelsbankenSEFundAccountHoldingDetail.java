package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.rpc;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities.HandelsbankenSEFundDetails;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAmount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule.InstrumentType;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.id.InstrumentIdModule;

public class HandelsbankenSEFundAccountHoldingDetail extends BaseResponse {

    private String currency;
    private HandelsbankenAmount totalChange;
    private HandelsbankenSEFundDetails fundDetails;
    private String isin;
    private HandelsbankenAmount marketValue;
    private HandelsbankenAmount averageValueOfCost;

    public Optional<InstrumentModule> toInstrumentModule() {
        return Optional.ofNullable(marketValue)
                .map(HandelsbankenAmount::asDouble)
                .filter(marketValue -> marketValue != 0d)
                .map(
                        extractedMarketValue -> {
                            final Optional<HandelsbankenSEFundDetails> details =
                                    Optional.ofNullable(this.fundDetails);
                            final String name =
                                    details.map(HandelsbankenSEFundDetails::getName).orElse(null);
                            final String isin =
                                    Optional.ofNullable(this.isin)
                                            .orElse(
                                                    details.map(HandelsbankenSEFundDetails::getIsin)
                                                            .orElse(null));
                            final String marketPlace =
                                    details.map(HandelsbankenSEFundDetails::getExternalFundId)
                                            .orElse(null);
                            return InstrumentModule.builder()
                                    .withType(InstrumentType.FUND)
                                    .withId(InstrumentIdModule.of(isin, marketPlace, name, isin))
                                    .withMarketPrice(0.0)
                                    .withMarketValue(extractedMarketValue)
                                    .withAverageAcquisitionPrice(
                                            HandelsbankenAmount.asDoubleOrElseNull(
                                                    averageValueOfCost))
                                    .withCurrency(this.marketValue.getCurrency())
                                    .withQuantity(
                                            details.flatMap(
                                                            HandelsbankenSEFundDetails
                                                                    ::parseNavAmount)
                                                    .filter(navAmount -> navAmount != 0d)
                                                    .map(
                                                            navAmount ->
                                                                    extractedMarketValue
                                                                            / navAmount)
                                                    .orElse(null))
                                    .withProfit(totalChange.asDouble())
                                    .build();
                        });
    }
}
