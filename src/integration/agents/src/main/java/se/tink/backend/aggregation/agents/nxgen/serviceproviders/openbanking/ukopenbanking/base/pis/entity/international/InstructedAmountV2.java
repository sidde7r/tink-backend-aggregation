package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.entity.international;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class InstructedAmountV2 {
    private String amount;
    private String currency;

    public InstructedAmountV2() {}

    public InstructedAmountV2(ExactCurrencyAmount amount) {
        this.amount = amount.getStringValue(AmountEntity.getValueFormat());
        this.currency = amount.getCurrencyCode();
    }

    public ExactCurrencyAmount toTinkAmount() {
        return new ExactCurrencyAmount(new BigDecimal(amount), currency);
    }
}
