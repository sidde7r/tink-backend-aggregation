package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.SwedbankSeSerializationUtils;
import se.tink.backend.core.Amount;

@JsonIgnoreProperties(ignoreUnknown = true)
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
