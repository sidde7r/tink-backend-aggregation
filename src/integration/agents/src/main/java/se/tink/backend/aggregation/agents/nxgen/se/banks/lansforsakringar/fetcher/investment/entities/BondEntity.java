package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.LansforsakringarConstants.Accounts;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule.InstrumentType;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.id.InstrumentIdModule;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class BondEntity {
    private String name;
    private String isinCode;
    private String marketValue;
    private String numberOfUnits;
    private String acquisitionCostPerSecurity;
    private String acquisitionCost;
    private String growthInPercent;
    private boolean watched;

    public String getName() {
        return name;
    }

    public String getIsinCode() {
        return isinCode;
    }

    public String getMarketValue() {
        return marketValue;
    }

    public String getNumberOfUnits() {
        return numberOfUnits;
    }

    public String getAcquisitionCostPerSecurity() {
        return acquisitionCostPerSecurity;
    }

    public String getAcquisitionCost() {
        return acquisitionCost;
    }

    public String getGrowthInPercent() {
        return growthInPercent;
    }

    public boolean isWatched() {
        return watched;
    }

    @JsonIgnore
    public InstrumentModule toTinkInstrument() {
        double quantity = StringUtils.parseAmount(numberOfUnits);
        double instrumentMarketValue = StringUtils.parseAmount(marketValue);
        double instrumentAcquisitionCost = StringUtils.parseAmount(acquisitionCost);
        return InstrumentModule.builder()
                .withType(InstrumentType.OTHER)
                .withId(getIdModule())
                .withMarketPrice(instrumentMarketValue / quantity == 0.0 ? 1 : quantity)
                .withMarketValue(instrumentMarketValue)
                .withAverageAcquisitionPrice(instrumentMarketValue / quantity)
                .withCurrency(Accounts.CURRENCY)
                .withQuantity(quantity)
                .withProfit(instrumentMarketValue - instrumentAcquisitionCost)
                .build();
    }

    private InstrumentIdModule getIdModule() {
        return InstrumentIdModule.builder()
                .withUniqueIdentifier(isinCode + name.replaceAll(" ", ""))
                .withName(name)
                .setIsin(isinCode)
                .build();
    }
}
