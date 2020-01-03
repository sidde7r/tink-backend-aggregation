package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAmount;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule.InstrumentType;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.id.InstrumentIdModule;

@JsonObject
public class FundHolding {

    private FundHoldingDetail fundHoldingDetail;
    private String fundName;
    // Not sure that the following fields are always returned...
    // They are not part of Handelsbanken App, but seem to be part of the server response at the
    // moment (2018-01-19)
    private String currency;
    private String isin;
    private String companyName;

    public Optional<InstrumentModule> toInstrumentModule() {
        if (Objects.isNull(fundHoldingDetail) || fundHoldingDetail.hasNoValue()) {
            return Optional.empty();
        }

        return Optional.of(
                InstrumentModule.builder()
                        .withType(InstrumentType.FUND)
                        .withId(InstrumentIdModule.of(isin, null, fundName, isin))
                        .withMarketPrice(
                                HandelsbankenAmount.asDoubleOrElseNull(
                                        fundHoldingDetail.getPrice()))
                        .withMarketValue(fundHoldingDetail.getMarketValueFormatted())
                        .withAverageAcquisitionPrice(
                                HandelsbankenAmount.asDoubleOrElseNull(
                                        fundHoldingDetail.getAverageValueOfCost()))
                        .withCurrency(currency)
                        .withQuantity(fundHoldingDetail.getHoldingUnits())
                        .withProfit(
                                HandelsbankenAmount.asDoubleOrElseNull(
                                        fundHoldingDetail.getTotalChange()))
                        .setRawType(companyName)
                        .build());
    }

    public String getIsin() {
        return isin;
    }

    @JsonIgnore
    public Optional<String> getFundAccount() {
        if (Objects.isNull(fundHoldingDetail)) {
            return Optional.empty();
        }
        return Optional.of(fundHoldingDetail.getAccount());
    }
}
