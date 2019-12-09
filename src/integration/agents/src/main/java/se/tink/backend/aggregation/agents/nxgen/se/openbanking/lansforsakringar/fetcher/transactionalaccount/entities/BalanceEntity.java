package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class BalanceEntity {

    @JsonIgnore static Amount Default = Amount.inSEK(0);

    private BalanceAmountEntity balanceAmount;
    private String balanceType;

    public String getBalanceType() {
        return balanceType;
    }

    public Amount toAmount() {
        return balanceAmount.toAmount();
    }

    public boolean isAvailableBalance() {
        return balanceType.equalsIgnoreCase("AUTHORIZED");
    }
}
