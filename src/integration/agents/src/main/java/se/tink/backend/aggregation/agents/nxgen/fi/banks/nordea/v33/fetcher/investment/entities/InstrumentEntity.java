package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.NordeaFIConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class InstrumentEntity {
    private String id;
    private double price;
    private String currency;
    private String instrumentName;

    @JsonProperty("instrument_type")
    @Getter
    private String rawType;

    private String isin;

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
}
