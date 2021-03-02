package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.investment.entities;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1AmountUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule.InstrumentType;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.id.InstrumentIdModule;

@JsonObject
@Getter
public class HoldingsEntity {
    private String productId;
    private String productName;
    private String isin;
    private String marketValueInteger;
    private String marketValueFraction;
    private String costPriceInteger;
    private String costPriceFraction;
    private String rateOfInvestCurrencyInteger;
    private String rateOfInvestCurrencyFraction;
    private String sharesInteger;
    private String sharesFraction;

    public InstrumentModule toTinkInstrument() {
        return InstrumentModule.builder()
                .withType(InstrumentType.FUND)
                .withId(buildInstrumentIdModule())
                .withMarketPrice(getPrice())
                .withMarketValue(getMarketValue())
                .withAverageAcquisitionPrice(null)
                .withCurrency("NOK")
                .withQuantity(getQuantity())
                .withProfit(getProfit())
                .build();
    }

    private InstrumentIdModule buildInstrumentIdModule() {
        return InstrumentIdModule.builder()
                .withUniqueIdentifier(productId)
                .withName(productName)
                .setIsin(isin)
                .build();
    }

    private Double getMarketValue() {
        return Sparebank1AmountUtils.constructDouble(marketValueInteger, marketValueFraction);
    }

    private Double getPrice() {
        return Sparebank1AmountUtils.constructDouble(costPriceInteger, costPriceFraction);
    }

    private Double getProfit() {
        return Sparebank1AmountUtils.constructDouble(
                rateOfInvestCurrencyInteger, rateOfInvestCurrencyFraction);
    }

    private Double getQuantity() {
        return Sparebank1AmountUtils.constructDouble(sharesInteger, sharesFraction);
    }
}
