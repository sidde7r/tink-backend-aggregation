package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProfitabilityItemEntity {
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
        double purchaseAmount = acquisitionValue.getFreeAmount().getAmount() +
                (acquisitionValue.getValue().getAmount() * quantity);

        return quoteAmount.getAmount() - purchaseAmount;
    }
}
