package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.SwedbankSeSerializationUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AmountEntity {
    protected String amount;
    protected String currencyCode;

    public String getAmount() {
        return amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    @JsonIgnore
    public Amount getTinkAmount() {
        return SwedbankSeSerializationUtils.parseAmountForInput(amount, currencyCode);
    }
}
