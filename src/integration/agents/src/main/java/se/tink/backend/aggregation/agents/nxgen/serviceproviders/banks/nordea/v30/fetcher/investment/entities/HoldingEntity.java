package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;

@JsonObject
public class HoldingEntity {
    @JsonProperty("market_value")
    private double marketValue;

    @JsonProperty("profit_loss")
    private double profitLoss;

    @JsonProperty private String id;
    @JsonProperty private double quantity;

    @JsonProperty("avg_purchase_price")
    private double avgPurchasePrice;

    @JsonProperty private InstrumentEntity instrument;

    public Optional<InstrumentModule> toTinkInstrument() {
        return Optional.of(instrument.applyTo(marketValue, profitLoss, quantity, avgPurchasePrice));
    }

    public boolean isInstrument() {
        return NordeaBaseConstants.INSTRUMENT_TYPE_MAP
                .translate(instrument.getRawType())
                .isPresent();
    }
}
