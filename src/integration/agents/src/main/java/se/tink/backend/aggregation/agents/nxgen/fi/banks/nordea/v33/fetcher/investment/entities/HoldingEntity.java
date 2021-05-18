package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.investment.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class HoldingEntity {
    private double marketValue;
    private double profitLoss;
    private String id;
    private double quantity;
    private double avgPurchasePrice;
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
        return !"CASH".equalsIgnoreCase(instrument.getRawType());
    }
}
