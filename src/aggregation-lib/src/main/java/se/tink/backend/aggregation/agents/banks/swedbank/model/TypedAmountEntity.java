package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TypedAmountEntity {
    private String type;
    private AmountEntity nominalValue;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public AmountEntity getNominalValue() {
        return nominalValue;
    }

    public void setNominalValue(AmountEntity nominalValue) {
        this.nominalValue = nominalValue;
    }
}
