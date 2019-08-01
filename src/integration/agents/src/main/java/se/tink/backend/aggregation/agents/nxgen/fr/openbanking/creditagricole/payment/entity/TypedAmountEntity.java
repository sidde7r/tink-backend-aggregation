package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TypedAmountEntity {
    @JsonProperty("type")
    private String type = null;

    @JsonProperty("amount")
    private AmountTypeEntity amount = null;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public AmountTypeEntity getAmount() {
        return amount;
    }

    public void setAmount(AmountTypeEntity amount) {
        this.amount = amount;
    }
}
