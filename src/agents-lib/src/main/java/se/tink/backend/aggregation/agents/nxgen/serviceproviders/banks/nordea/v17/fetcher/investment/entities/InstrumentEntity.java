package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.NordeaV17Constants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class InstrumentEntity {
    private InstrumentIdEntity instrumentId;
    private String instrumentName;
    private String instrumentType;
    private String price;
    private String priceTime;
    private String todaysChange;
    @JsonProperty("todaysChangePct")
    private String todaysChangePercentage;

    public InstrumentIdEntity getInstrumentId() {
        return instrumentId;
    }

    public String getInstrumentName() {
        return instrumentName;
    }

    public String getInstrumentType() {
        return instrumentType;
    }

    public Double getPrice() {
        return price == null || price.isEmpty() ? null : StringUtils.parseAmount(price);
    }

    public String getPriceTime() {
        return priceTime;
    }

    public String getTodaysChange() {
        return todaysChange;
    }

    public String getTodaysChangePercentage() {
        return todaysChangePercentage;
    }

    public Instrument.Type getTinkInstrumentType() {
        switch (getInstrumentType().toLowerCase()) {
        case NordeaV17Constants.Investments.InstrumentTypes.EQUITY:
            return Instrument.Type.STOCK;
        case NordeaV17Constants.Investments.InstrumentTypes.FUND:
            return Instrument.Type.FUND;
        case NordeaV17Constants.Investments.InstrumentTypes.DERIVATIVE:
            // Intentional fall through
        default:
            return Instrument.Type.OTHER;
        }
    }
}
