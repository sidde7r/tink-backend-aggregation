package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.SparebankenVestConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
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
    public Instrument toTinkInstrument() {
        Instrument instrument = new Instrument();
        instrument.setIsin(this.isinnr);
        instrument.setMarketValue(this.verdi);
        instrument.setName(this.navn);
        instrument.setRawType(this.type);
        instrument.setType(getTinkInstrumentType());
        instrument.setProfit(this.gevinst);
        instrument.setUniqueIdentifier(this.isinnr);
        instrument.setPrice(this.kostpris);

        return instrument;
    }

    @JsonIgnore
    private Instrument.Type getTinkInstrumentType() {
        if (isFund() || isPension()) {
            return Instrument.Type.FUND;
        }

        return Instrument.Type.OTHER;
    }

    public Double getGevinst() {
        return this.gevinst;
    }

    public String getIsinnr() {
        return this.isinnr;
    }

    public String getKontonummer() {
        return this.kontonummer;
    }

    public Double getVerdi() {
        return this.verdi;
    }

    public String getType() {
        return this.type;
    }

    @JsonIgnore
    public Portfolio.Type getTinkPortfolioType() {
        if (isPension()) {
            return Portfolio.Type.PENSION;
        } else if (isFund()) {
            return Portfolio.Type.DEPOT;
        } else {
            return Portfolio.Type.OTHER;
        }
    }

    private boolean isPension() {
        return SparebankenVestConstants.Investments.PENSION_PORTFOLIO_TYPE.equalsIgnoreCase(
                this.type);
    }

    private boolean isFund() {
        return SparebankenVestConstants.Investments.FUND_TYPE.equalsIgnoreCase(this.type);
    }
}
