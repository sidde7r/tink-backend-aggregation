package se.tink.agent.sdk.models.account;

import lombok.Getter;
import se.tink.agent.sdk.models.Amount;

@Getter
public class AccountCredit {
    private final AccountCreditType type;
    private final Amount amount;

    AccountCredit(AccountCreditType type, Amount amount) {
        this.type = type;
        this.amount = amount;
    }

    public static AccountCredit from(AccountCreditType type, Amount amount) {
        return new AccountCredit(type, amount);
    }
}
