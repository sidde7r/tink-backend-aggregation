package se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.BawagConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BalanceEntity {
    private BalanceAmountEntity balanceAmount;
    private String balanceType;

    public BalanceEntity() {}

    BalanceEntity(final BalanceAmountEntity balanceAmount, final String balanceType) {
        this.balanceAmount = balanceAmount;
        this.balanceType = balanceType;
    }

    public boolean isClosingBooked() {
        return balanceType.equalsIgnoreCase(BawagConstants.Accounts.BALANCE_CLOSING_BOOKED)
                || balanceType.equalsIgnoreCase(BawagConstants.Accounts.CLBD);
    }

    public boolean isInCurrency(final String currency) {
        return balanceAmount.getCurrency().equalsIgnoreCase(currency);
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
}
