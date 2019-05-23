package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.entity.international;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class InstructedAmount {
    private AmountEntity amount;

    public InstructedAmount() {}

    public InstructedAmount(Amount amount) {
        this.amount = new AmountEntity(amount);
    }

    public Amount toTinkAmount() {
        return amount.toTinkAmount();
    }
}
