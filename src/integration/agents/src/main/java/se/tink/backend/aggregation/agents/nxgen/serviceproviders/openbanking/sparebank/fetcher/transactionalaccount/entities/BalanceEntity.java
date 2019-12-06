package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.Accounts;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BalanceEntity {
    private BalanceAmountEntity balanceAmount;
    private String balanceType;

    public BalanceEntity() {}

    public BalanceEntity(final BalanceAmountEntity balanceAmount, final String balanceType) {
        this.balanceAmount = balanceAmount;
        this.balanceType = balanceType;
    }

    public ExactCurrencyAmount toAmount() {
        return balanceAmount.toAmount();
    }

    public BalanceAmountEntity getBalanceAmount() {
        return balanceAmount;
    }

    public String getBalanceType() {
        return balanceType;
    }

    public boolean isClosingBooked() {
        return balanceType.equalsIgnoreCase(Accounts.BALANCE_CLOSING_BOOKED);
    }

    public boolean isInCurrency(final String currency) {
        return balanceAmount.getCurrency().equalsIgnoreCase(currency);
    }
}
