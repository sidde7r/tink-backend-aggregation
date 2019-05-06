package se.tink.backend.aggregation.agents.nxgen.be.openbanking.crelan.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.crelan.CrelanConstants.BalanceTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class BalanceEntity {
    public static final Amount DEFAULT = Amount.inEUR(0);

    private AmountEntity balanceAmount;
    private String balanceType;
    private String lastChangeDateTime;
    private String referenceDate;

    @JsonIgnore
    public boolean isAvailableBalance() {
        return balanceType.equalsIgnoreCase(BalanceTypes.INTERIM_AVAILABLE);
    }

    @JsonIgnore
    public Amount toAmount() {
        return balanceAmount.toAmount();
    }
}
