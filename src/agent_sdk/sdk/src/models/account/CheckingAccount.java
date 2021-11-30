package se.tink.agent.sdk.models.account;

import lombok.Getter;
import se.tink.agent.sdk.models.account.builder.AccountBuilder;

@Getter
public class CheckingAccount {
    private final Account account;

    CheckingAccount(Account account) {
        this.account = account;
    }

    public static AccountBuilder.BankReferenceBuilder<CheckingAccount> builder() {
        return new AccountBuilder.BankReferenceBuilder<>(CheckingAccount::new);
    }
}
