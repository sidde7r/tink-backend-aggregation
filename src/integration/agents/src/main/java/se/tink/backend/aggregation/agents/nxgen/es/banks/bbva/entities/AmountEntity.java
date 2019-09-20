package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import io.vavr.control.Option;
import java.math.BigDecimal;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.Defaults;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AmountEntity {
    private BigDecimal amount;
    private String currency;

    @JsonSetter
    public void setCurrency(JsonNode jsonNode) {
        this.currency =
                Option.of(jsonNode.get("id"))
                        .map(JsonNode::asText)
                        .getOrElse(jsonNode.asText(Defaults.CURRENCY));
    }

    @JsonIgnore
    public ExactCurrencyAmount toTinkAmount() {
        return ExactCurrencyAmount.of(amount, currency);
    }

    @JsonIgnore
    public double getAmount() {
        return amount.doubleValue();
    }
}
