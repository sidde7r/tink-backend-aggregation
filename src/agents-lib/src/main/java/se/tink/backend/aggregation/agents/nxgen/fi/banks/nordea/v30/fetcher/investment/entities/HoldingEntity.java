package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.system.rpc.Instrument;

@JsonObject
public class HoldingEntity {

    @JsonProperty("market_value")
    private double marketValue;

    @JsonProperty("profit_loss")
    private double profitLoss;

    private String id;

    private double quantity;

    @JsonProperty("avg_purchase_price")
    private double avgPurchasePrice;

    private InstrumentEntity instrument;

    public Instrument toTinkInstrument(){

        Instrument tinkInstrument = instrument.toTinkInstrument();
        tinkInstrument.setAverageAcquisitionPrice(avgPurchasePrice);
        tinkInstrument.setQuantity(quantity);
        tinkInstrument.setProfit(profitLoss);
        tinkInstrument.setMarketValue(marketValue);

        return tinkInstrument;
    }
}
