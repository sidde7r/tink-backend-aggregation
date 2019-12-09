package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.BalanceTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class BalanceEntity {
    public static final Amount defaultAmount = Amount.inEUR(0);

    private AmountEntity balanceAmount;
    private String balanceType;
    private String referenceDate;

    public boolean isAvailableBalance() {
        return balanceType.equalsIgnoreCase(BalanceTypes.AVAILABLE);
    }

    public Amount getAmount() {
        return balanceAmount.toAmount();
    }
}
