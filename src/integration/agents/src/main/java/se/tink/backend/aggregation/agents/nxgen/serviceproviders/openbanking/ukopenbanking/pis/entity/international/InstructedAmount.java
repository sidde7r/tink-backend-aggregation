package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.entity.international;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class InstructedAmount {
    private AmountEntity amount;

    public InstructedAmount() {}

    public InstructedAmount(ExactCurrencyAmount amount) {
        this.amount = new AmountEntity(amount);
    }

    public ExactCurrencyAmount toTinkAmount() {
        return amount.toTinkAmount();
    }
}
