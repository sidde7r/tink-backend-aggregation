package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TotalFundAccountEntity {
    private AmountEntity value;

    public AmountEntity getValue() {
        return value;
    }

    public void setValue(AmountEntity value) {
        this.value = value;
    }
}
