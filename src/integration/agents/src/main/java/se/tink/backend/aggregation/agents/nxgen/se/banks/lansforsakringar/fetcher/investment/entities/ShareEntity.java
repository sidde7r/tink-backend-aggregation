package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.LansforsakringarConstants.Accounts;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule.InstrumentType;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.id.InstrumentIdModule;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class ShareEntity {
    private String name;
    private String isinCode;
    private String marketValue;
    private String numberOfUnits;
    private String acquisitionCostPerSecurity;
    private String acquisitionCost;
    private String growthInPercent;
    private Boolean watched;

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

    public Boolean getWatched() {
        return watched;
    }

    @JsonIgnore
    public InstrumentModule toTinkInstrument(Optional<InstrumentDetailsEntity> instrumentDetails) {
        return InstrumentModule.builder()
                .withType(InstrumentType.STOCK)
                .withId(getIdModule(instrumentDetails))
                .withMarketPrice(
                        instrumentDetails
                                .map(InstrumentDetailsEntity::getBuyingPrice)
                                .map(StringUtils::parseAmount)
                                .orElse(0.0))
                .withMarketValue(StringUtils.parseAmount(marketValue))
                .withAverageAcquisitionPrice(StringUtils.parseAmount(acquisitionCostPerSecurity))
                .withCurrency(
                        instrumentDetails
                                .map(InstrumentDetailsEntity::getCurrency)
                                .orElse(Accounts.CURRENCY))
                .withQuantity(StringUtils.parseAmount(numberOfUnits))
                .withProfit(
                        StringUtils.parseAmount(marketValue)
                                - StringUtils.parseAmount(acquisitionCost))
                .build();
    }

    private InstrumentIdModule getIdModule(Optional<InstrumentDetailsEntity> instrumentDetails) {
        return InstrumentIdModule.builder()
                .withUniqueIdentifier(
                        isinCode
                                + instrumentDetails
                                        .map(InstrumentDetailsEntity::getSymbol)
                                        .map(String::trim)
                                        .orElse(""))
                .withName(name)
                .setIsin(isinCode)
                .build();
    }
}
