package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.entity.domestic;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class InstructedAmount {
    private String amount;
    private String currency;

    // Used in serialization unit tests
    protected InstructedAmount() {}

    public InstructedAmount(ExactCurrencyAmount amount) {
        this.amount = amount.getStringValue();
        this.currency = amount.getCurrencyCode();
    }

    public ExactCurrencyAmount toTinkAmount() {
        return new ExactCurrencyAmount(new BigDecimal(amount), currency);
    }
}
