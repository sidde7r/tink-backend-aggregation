package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v11.pis.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class InstructedAmountEntity {
    @JsonProperty("Amount")
    private String amount;
    @JsonProperty("Currency")
    private String currency; // MUST BE "GBP"

    private InstructedAmountEntity(@JsonProperty("Amount") String amount,
            @JsonProperty("Currency") String currency) {
        this.amount = amount;
        this.currency = currency;
    }

    @JsonIgnore
    public static InstructedAmountEntity create(Amount tinkAmount) {
        return new InstructedAmountEntity(String.valueOf(tinkAmount.getValue()), tinkAmount.getCurrency());
    }
}
