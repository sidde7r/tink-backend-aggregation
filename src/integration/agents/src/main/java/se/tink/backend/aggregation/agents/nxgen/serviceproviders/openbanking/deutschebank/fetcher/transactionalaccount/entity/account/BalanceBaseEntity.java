package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.entity.account;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.Accounts;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.entity.transactions.BalanceAmountBaseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BalanceBaseEntity {
    private BalanceAmountBaseEntity balanceAmount;
    private String balanceType;

    public boolean isClosingBooked() {
        return balanceType.equalsIgnoreCase(Accounts.BALANCE_CLOSING_BOOKED)
                || balanceType.equalsIgnoreCase(Accounts.CLBD);
    }

    public boolean isInCurrency(final String currency) {
        return balanceAmount.getCurrency().equalsIgnoreCase(currency);
    }

    public ExactCurrencyAmount toAmount() {
        return balanceAmount.toAmount();
    }

    public BalanceAmountBaseEntity getBalanceAmount() {
        return balanceAmount;
    }

    public String getBalanceType() {
        return balanceType;
    }
}
