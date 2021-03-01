package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.RaiffeisenConstants.BalanceTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BalanceEntity {

    public static ExactCurrencyAmount Default = ExactCurrencyAmount.zero("HUF");
    private BalanceAmountEntity balanceAmount;
    private String balanceType;
    private String referenceDate;

    public boolean isAvailableBalance() {
        return balanceType.equalsIgnoreCase(BalanceTypes.INTERIM_BOOKED);
    }

    public ExactCurrencyAmount toAmount() {
        return balanceAmount.toAmount();
    }
}
