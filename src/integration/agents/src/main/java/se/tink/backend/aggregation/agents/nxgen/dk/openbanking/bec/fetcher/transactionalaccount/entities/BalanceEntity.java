package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.bec.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class BalanceEntity {
    @JsonIgnore public static Amount Default = Amount.inDKK(0);
    private String balanceType;
    private AmountEntity balanceAmount;

    public boolean isAvailable() {
        return balanceType.equalsIgnoreCase("expected");
    }

    public Amount getAmount() {
        return balanceAmount.toAmount();
    }
}
