package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private Double gevinst;
    private String isinnr;
    private String kontonummer;
    private Double kostpris;
    private String navn;
    private Double sparebeloep;
    private Double verdi;
    private String type;
    private String portefoljeNavn;

    @JsonIgnore
    public String getPortfolioName() {
        return Strings.isNullOrEmpty(this.portefoljeNavn) ? this.kontonummer : this.portefoljeNavn;
    }

    @JsonIgnore
    public InstrumentModule toTinkInstrument() {
        return InstrumentModule.builder()
                .withType(getTinkInstrumentType())
                .withId(prepareIdModule())
                .withMarketPrice(kostpris != null ? kostpris : 0)
                .withMarketValue(verdi)
                .withAverageAcquisitionPrice(null) // not possible to get
                .withCurrency("NOK")
                .withQuantity(1) // not possible to get
                .withProfit(gevinst)
                .setRawType(type)
                .build();
    }

    private InstrumentIdModule prepareIdModule() {
        return InstrumentIdModule.of(isinnr, null, navn, isinnr);
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
