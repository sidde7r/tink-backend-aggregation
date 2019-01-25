package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.SparebankenVestConstants;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public class FundInvestmentEntity {
    private static final AggregationLogger LOGGER = new AggregationLogger(FundInvestmentEntity.class);

    private double gevinst;
    private String isinnr;
    private String kontonummer;
    private double kostpris;
    private String navn;
    private double sparebeloep;
    private double verdi;
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

        return instrument;
    }

    @JsonIgnore
    private Instrument.Type getTinkInstrumentType() {
        if (isFund() || isPension()) {
            return Instrument.Type.FUND;
        }

        // check if there are any other types than FUND and PENSION FUND
        LOGGER.infoExtraLong("Unknown investment type: " + SerializationUtils.serializeToString(this),
                SparebankenVestConstants.LogTags.INVESTMENTS);

        return Instrument.Type.OTHER;
    }

    public double getGevinst() {
        return this.gevinst;
    }

    public String getIsinnr() {
        return this.isinnr;
    }

    public String getKontonummer() {
        return this.kontonummer;
    }

    public double getVerdi() {
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
        return SparebankenVestConstants.Investments.PENSION_PORTFOLIO_TYPE.equalsIgnoreCase(this.type);
    }

    private boolean isFund() {
        return SparebankenVestConstants.Investments.FUND_TYPE.equalsIgnoreCase(this.type);
    }
}
