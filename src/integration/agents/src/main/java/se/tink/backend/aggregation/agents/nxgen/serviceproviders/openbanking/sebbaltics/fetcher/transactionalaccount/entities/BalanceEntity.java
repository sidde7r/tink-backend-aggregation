package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.fetcher.transactionalaccount.entities;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.SebBalticsConstants.Accounts;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BalanceEntity {

    private String balanceType;
    private boolean creditLimitIncluded;
    private BalanceAmountEntity balanceAmount;

    public boolean isAvailableBalance() {
        return balanceType.equalsIgnoreCase(Accounts.AVAILABLE_BALANCE);
    }

    public boolean isBookedBalance() {
        return balanceType.equalsIgnoreCase(Accounts.BOOKED_BALANCE);
    }

    public ExactCurrencyAmount toAmount() {
        return Optional.ofNullable(balanceAmount)
                .map(balance -> new ExactCurrencyAmount(balance.getAmount(), balance.getCurrency()))
                .orElseThrow(() -> new IllegalStateException("Could not parse amount"));
    }
}
