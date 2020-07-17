package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class BalanceEntity {

    @JsonIgnore static final Amount DEFAULT = Amount.inSEK(0);

    private BalanceAmountEntity balanceAmount;
    private String balanceType;

    public String getBalanceType() {
        return balanceType;
    }

    public BalanceAmountEntity getBalanceAmount() {
        return balanceAmount;
    }

    public Amount toAmount() {
        return balanceAmount.toAmount();
    }

    public boolean isAvailableBalance() {
        return balanceType.equalsIgnoreCase("AUTHORIZED");
    }
}
