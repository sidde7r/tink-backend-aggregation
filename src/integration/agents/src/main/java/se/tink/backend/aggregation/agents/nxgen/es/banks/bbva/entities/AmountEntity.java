package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import io.vavr.control.Option;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.Defaults;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AmountEntity {
    private double amount;
    private String currency;

    public double getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    @JsonSetter
    public void setCurrency(JsonNode jsonNode) {
        this.currency =
                Option.of(jsonNode.get("id"))
                        .map(JsonNode::asText)
                        .getOrElse(jsonNode.asText(Defaults.CURRENCY));
    }

    @JsonIgnore
    public Amount toTinkAmount() {
        return new Amount(getCurrency(), amount);
    }
}
