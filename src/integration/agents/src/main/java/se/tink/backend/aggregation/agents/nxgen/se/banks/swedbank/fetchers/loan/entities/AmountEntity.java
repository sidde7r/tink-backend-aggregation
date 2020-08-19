package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.utils.SwedbankSeSerializationUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

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
    public ExactCurrencyAmount getTinkAmount() {
        return SwedbankSeSerializationUtils.parseAmountForInput(amount, currencyCode);
    }
}
