package se.tink.agent.sdk.models.account;

import lombok.Getter;
import se.tink.agent.sdk.models.account.builder.AccountBuilder;

@Getter
public class SavingsAccount {
    private final Account account;

    SavingsAccount(Account account) {
        this.account = account;
    }

    public static AccountBuilder.BankReferenceBuilder<SavingsAccount> builder() {
        return new AccountBuilder.BankReferenceBuilder<>(SavingsAccount::new);
    }
}
