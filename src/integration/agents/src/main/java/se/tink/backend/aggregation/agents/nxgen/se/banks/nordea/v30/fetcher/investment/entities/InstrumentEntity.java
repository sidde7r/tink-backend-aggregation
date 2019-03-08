package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InstrumentEntity {
    @JsonProperty private String id;
    @JsonProperty private double price;
    @JsonProperty private String currency;
    @JsonProperty private String market;

    @JsonProperty("instrument_name")
    private String instrumentName;

    @JsonProperty("instrument_type")
    private String rawType;

    @JsonProperty private String isin;

    @JsonIgnore
    public Instrument toTinkInstrument() {

        Instrument instrument = new Instrument();

        instrument.setUniqueIdentifier(isin + market);
        instrument.setIsin(isin);
        instrument.setMarketPlace(market);
        instrument.setCurrency(currency);
        instrument.setName(instrumentName);
        instrument.setPrice(price);
        instrument.setType(
                NordeaSEConstants.INSTRUMENT_TYPE_MAP
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

    @JsonIgnore
    public String getRawType() {
        return rawType;
    }
}
