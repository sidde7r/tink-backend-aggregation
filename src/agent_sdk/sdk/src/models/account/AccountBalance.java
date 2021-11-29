package se.tink.agent.sdk.models.account;

import lombok.Getter;
import se.tink.agent.sdk.models.Amount;

@Getter
public class AccountBalance {
    private final AccountBalanceType type;
    private final Amount amount;

    AccountBalance(AccountBalanceType type, Amount amount) {
        this.type = type;
        this.amount = amount;
    }

    public static AccountBalance from(AccountBalanceType type, Amount amount) {
        return new AccountBalance(type, amount);
    }
}
