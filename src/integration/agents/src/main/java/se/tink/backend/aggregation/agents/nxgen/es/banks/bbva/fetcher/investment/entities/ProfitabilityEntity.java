package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProfitabilityEntity {
    @JsonProperty("titles")
    private double quantity;

    private AmountEntity dividenAmount;
    private AmountEntity quoteAmount;

    @JsonProperty("inbound")
    private ValueEntity acquisitionValue;

    @JsonProperty("outbound")
    private ValueEntity soldValue;

    @JsonIgnore
    public double getTotalProfit() {
        double purchaseAmount =
                acquisitionValue.getFreeAmount().getAmountAsDouble()
                        + (acquisitionValue.getValue().getAmountAsDouble() * quantity);

        return quoteAmount.getAmountAsDouble() - purchaseAmount;
    }
}
