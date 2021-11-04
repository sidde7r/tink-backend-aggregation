package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.utils.SwedbankSeSerializationUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Getter
public class AmountEntity {
    protected String amount;
    protected String currencyCode;

    @JsonIgnore
    public ExactCurrencyAmount getTinkAmount() {
        return SwedbankSeSerializationUtils.parseAmountForInput(amount, currencyCode);
    }
}
