package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.NordeaFiConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InstrumentEntity {

    private String id;

    private double price;

    private String currency;

    @JsonProperty("instrument_name")
    private String instrumentName;

    @JsonProperty("instrument_type")
    private String rawType;

    private String isin;

    private String market; // This is always null. Market can be extracted from id instead.

    public Instrument toTinkInstrument() {

        Instrument instrument = new Instrument();

        market = getMarket();

        instrument.setUniqueIdentifier(isin + market);
        instrument.setIsin(isin);
        instrument.setMarketPlace(market);
        instrument.setCurrency(currency);
        instrument.setName(instrumentName);
        instrument.setPrice(price);
        instrument.setType(NordeaFiConstants.GET_INSTRUMENT_TYPE(rawType));
        instrument.setRawType(rawType);

        // The following fields are set in HoldingEntity since the can't be found here.
        // AvgPurchasePrice
        // Quantity
        // Profit
        // MarketValue

        return instrument;
    }

    private String getMarket() {

        // id format TRADER-ISIN-MARKET-ZONE
        return id.split("-")[2];
    }
}
