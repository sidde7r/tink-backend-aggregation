package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.RaiffeisenConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class BalanceEntity {

    public static Amount Default = new Amount();
    private BalanceAmountEntity balanceAmount;
    private String balanceType;
    private String referenceDate;

    public boolean isAvailableBalance() {
        return balanceType.equalsIgnoreCase(RaiffeisenConstants.BalanceTypes.INTERIM_BOOKED);
    }

    public Amount toAmount() {
        return balanceAmount.toAmount();
    }
}
