package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.NordeaFIConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InstrumentEntity {
    @JsonProperty private String id;
    @JsonProperty private double price;
    @JsonProperty private String currency;

    @JsonProperty("instrument_name")
    private String instrumentName;

    @JsonProperty("instrument_type")
    private String rawType;

    @JsonProperty private String isin;

    public Instrument toTinkInstrument() {
        Instrument instrument = new Instrument();

        instrument.setUniqueIdentifier(isin + getMarket());
        instrument.setIsin(isin);
        instrument.setMarketPlace(getMarket());
        instrument.setCurrency(currency);
        instrument.setName(instrumentName);
        instrument.setPrice(price);
        instrument.setType(
                NordeaFIConstants.INSTRUMENT_TYPE_MAP
                        .translate(rawType)
                        .orElse(Instrument.Type.OTHER));
        instrument.setRawType(rawType);
        // The following fields are set in HoldingEntity since the can't be found here.
        // AvgPurchasePrice
        // Quantity
        // Profit
        // MarketValue
        return instrument;
    }

    private String getMarket() {
        return idFormat();
    }

    private String idFormat() {
        // id format TRADER-ISIN-MARKET-ZONE
        return id.split("-")[2];
    }

    public String getRawType() {
        return rawType;
    }
}
