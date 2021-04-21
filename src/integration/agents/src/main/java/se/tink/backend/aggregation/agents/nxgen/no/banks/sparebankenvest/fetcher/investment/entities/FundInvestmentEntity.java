package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.SparebankenVestConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.id.InstrumentIdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;

@JsonObject
@Getter
public class FundInvestmentEntity {
    @JsonProperty("gevinst")
    private Double profit;

    private String isinnr;

    @JsonProperty("kontonummer")
    private String accountNumber;

    @JsonProperty("kostpris")
    private Double marketPrice;

    @JsonProperty("navn")
    private String name;

    @JsonProperty("sparebeloep")
    private Double savingsAmount;

    @JsonProperty("verdi")
    private Double value;

    private String type;

    @JsonProperty("portefoljeNavn")
    private String portfolioName;

    @JsonIgnore
    public String getPortfolioName() {
        return Strings.isNullOrEmpty(this.portfolioName) ? this.accountNumber : this.portfolioName;
    }

    @JsonIgnore
    public InstrumentModule toTinkInstrument() {
        return InstrumentModule.builder()
                .withType(getTinkInstrumentType())
                .withId(prepareIdModule())
                .withMarketPrice(marketPrice != null ? marketPrice : 0)
                .withMarketValue(value)
                .withAverageAcquisitionPrice(null) // not possible to get
                .withCurrency("NOK")
                .withQuantity(1) // not possible to get
                .withProfit(profit)
                .setRawType(type)
                .build();
    }

    private InstrumentIdModule prepareIdModule() {
        return InstrumentIdModule.of(isinnr, null, name, isinnr);
    }

    @JsonIgnore
    private InstrumentModule.InstrumentType getTinkInstrumentType() {
        if (isFund() || isPension()) {
            return InstrumentModule.InstrumentType.FUND;
        }
        if (isStockOption()) {
            return InstrumentModule.InstrumentType.STOCK;
        }

        return InstrumentModule.InstrumentType.OTHER;
    }

    @JsonIgnore
    public PortfolioModule.PortfolioType getTinkPortfolioType() {
        if (isPension()) {
            return PortfolioModule.PortfolioType.PENSION;
        } else if (isFund()) {
            return PortfolioModule.PortfolioType.DEPOT;
        } else {
            return PortfolioModule.PortfolioType.OTHER;
        }
    }

    private boolean isPension() {
        return SparebankenVestConstants.Investments.PENSION_PORTFOLIO_TYPE.equalsIgnoreCase(type);
    }

    private boolean isFund() {
        return SparebankenVestConstants.Investments.FUND_TYPE.equalsIgnoreCase(type);
    }

    private boolean isStockOption() {
        return SparebankenVestConstants.Investments.STOCK_OPTIONS_TYPE.equalsIgnoreCase(type);
    }
}
