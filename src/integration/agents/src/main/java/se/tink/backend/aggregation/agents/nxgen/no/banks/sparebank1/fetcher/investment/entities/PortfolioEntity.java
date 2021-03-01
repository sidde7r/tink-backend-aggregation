package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.investment.entities;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1AmountUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule.PortfolioType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;

@JsonObject
@Getter
public class PortfolioEntity {
    private String name;
    private String id;
    private String totalMarketValueInteger;
    private String totalMarketValueFraction;
    private String totalCostPriceInteger;
    private String totalCostPriceFraction;
    private String totalRateOfInvestCurrencyInteger;
    private String totalRateOfInvestCurrencyFraction;
    private Boolean periodicReportsIncluded;
    private List<HoldingsEntity> holdings;

    private Double getTotalMarketValue() {
        return Sparebank1AmountUtils.constructDouble(
                totalMarketValueInteger, totalMarketValueFraction);
    }

    private Double getTotalProfit() {
        return Sparebank1AmountUtils.constructDouble(
                totalRateOfInvestCurrencyInteger, totalRateOfInvestCurrencyFraction);
    }

    public InvestmentAccount toInvestmentAccount() {
        return InvestmentAccount.nxBuilder()
                .withPortfolios(buildPortfolioModule())
                .withZeroCashBalance("NOK")
                .withId(buildIdModule())
                .build();
    }

    private IdModule buildIdModule() {
        return IdModule.builder()
                .withUniqueIdentifier(id)
                .withAccountNumber(id)
                .withAccountName(name)
                .addIdentifier(AccountIdentifier.create(Type.OTHER, id))
                .build();
    }

    private PortfolioModule buildPortfolioModule() {
        return PortfolioModule.builder()
                .withType(PortfolioType.DEPOT)
                .withUniqueIdentifier(id)
                .withCashValue(0.0)
                .withTotalProfit(getTotalProfit())
                .withTotalValue(getTotalMarketValue())
                .withInstruments(buildInstruments())
                .build();
    }

    private List<InstrumentModule> buildInstruments() {
        return holdings.stream().map(HoldingsEntity::toTinkInstrument).collect(Collectors.toList());
    }
}
