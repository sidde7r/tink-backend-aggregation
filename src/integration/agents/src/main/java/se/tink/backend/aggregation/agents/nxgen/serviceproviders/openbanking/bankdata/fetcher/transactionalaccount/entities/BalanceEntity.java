package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

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
        return balanceType.equalsIgnoreCase(BankdataConstants.Accounts.BALANCE_CLOSING_BOOKED);
    }

    public boolean isInCurrency(final String currency) {
        return balanceAmount.getCurrency().equalsIgnoreCase(currency);
    }

    public Amount toAmount() {
        return balanceAmount.toAmount();
    }

    public BalanceAmountEntity getBalanceAmount() {
        return balanceAmount;
    }

    public String getBalanceType() {
        return balanceType;
    }
}
