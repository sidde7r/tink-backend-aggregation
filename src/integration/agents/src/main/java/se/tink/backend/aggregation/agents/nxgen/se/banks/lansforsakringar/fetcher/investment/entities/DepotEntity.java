package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule.PortfolioType;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class DepotEntity {
    private String depotNumber;
    private String totalGrowthInRealValue;
    private String totalValue;
    private String totalGrowthInPercent;

    public String getDepotNumber() {
        return depotNumber;
    }

    @JsonIgnore
    public PortfolioModule toTinkPortfolio(String cashValue, List<InstrumentModule> instruments) {
        return PortfolioModule.builder()
                .withType(PortfolioType.ISK)
                .withUniqueIdentifier(depotNumber)
                .withCashValue(StringUtils.parseAmount(cashValue))
                .withTotalProfit(StringUtils.parseAmount(totalGrowthInRealValue))
                .withTotalValue(StringUtils.parseAmount(totalValue))
                .withInstruments(instruments)
                .build();
    }
}
