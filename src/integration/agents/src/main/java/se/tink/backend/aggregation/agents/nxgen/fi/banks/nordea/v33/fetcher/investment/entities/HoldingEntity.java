package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class HoldingEntity {
    @JsonProperty("market_value")
    private double marketValue;
    @JsonProperty("profit_loss")
    private double profitLoss;
    @JsonProperty
    private String id;
    @JsonProperty
    private double quantity;
    @JsonProperty("avg_purchase_price")
    private double avgPurchasePrice;
    @JsonProperty
    private InstrumentEntity instrument;

    public Instrument toTinkInstrument() {
        Instrument tinkInstrument = instrument.toTinkInstrument();
        tinkInstrument.setAverageAcquisitionPrice(avgPurchasePrice);
        tinkInstrument.setQuantity(quantity);
        tinkInstrument.setProfit(profitLoss);
        tinkInstrument.setMarketValue(marketValue);

        return tinkInstrument;
    }

    public boolean isInstrument() {
        return !instrument.getRawType().equalsIgnoreCase("CASH");
    }
}
