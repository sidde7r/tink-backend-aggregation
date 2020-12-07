package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class BalanceEntity {

    private BalanceAmountEntity balanceAmount;
    private String balanceType;

    public boolean isAvailableBalance() {
        return balanceType.equalsIgnoreCase("AUTHORIZED");
    }
}
