package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.Accounts;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class BalanceBaseEntity {
    private BalanceAmountBaseEntity balanceAmount;
    private String balanceType;

    public boolean isClosingBooked() {
        return balanceType.equalsIgnoreCase(Accounts.BALANCE_CLOSING_BOOKED);
    }

    public boolean isInCurrency(final String currency) {
        return balanceAmount.getCurrency().equalsIgnoreCase(currency);
    }

    public Amount toAmount() {
        return balanceAmount.toAmount();
    }

    public BalanceAmountBaseEntity getBalanceAmount() {
        return balanceAmount;
    }

    public void setBalanceAmount(final BalanceAmountBaseEntity balanceAmount) {
        this.balanceAmount = balanceAmount;
    }

    public String getBalanceType() {
        return balanceType;
    }

    public void setBalanceType(final String balanceType) {
        this.balanceType = balanceType;
    }
}
