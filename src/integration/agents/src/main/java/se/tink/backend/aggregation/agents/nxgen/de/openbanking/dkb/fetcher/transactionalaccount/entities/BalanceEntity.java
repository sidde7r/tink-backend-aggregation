package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbConstants.BalanceTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class BalanceEntity {
    @JsonIgnore public static final Amount Default = Amount.inEUR(0);

    private AmountEntity balanceAmount;
    private String balanceType;
    private String lastChangeDateTime;
    private String lastCommittedTransaction;
    private String referenceDate;

    @JsonIgnore
    public boolean isAvailable() {
        return balanceType.equalsIgnoreCase(BalanceTypes.INTERIM_AVAILABLE)
                || balanceType.equalsIgnoreCase(BalanceTypes.FORWARD_AVAILABLE);
    }

    @JsonIgnore
    public Amount getAmount() {
        return balanceAmount.toAmount();
    }
}
