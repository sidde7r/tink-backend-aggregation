package se.tink.agent.sdk.models.account;

import lombok.Getter;
import se.tink.agent.sdk.models.account.builder.AccountBuilder;

@Getter
public class CreditCardAccount {
    private final Account account;

    CreditCardAccount(Account account) {
        this.account = account;
    }

    public static AccountBuilder.BankReferenceForCreditCardBuilder<CreditCardAccount> builder() {
        return new AccountBuilder.BankReferenceForCreditCardBuilder<>(CreditCardAccount::new);
    }
}
