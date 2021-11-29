package se.tink.agent.sdk.models.account;

import lombok.Getter;

@Getter
public class AccountHolder {
    private final AccountHolderRole role;
    private final String name;

    // Note: Not using lombok builder as we want to enforce that all values are set.
    AccountHolder(AccountHolderRole role, String name) {
        this.role = role;
        this.name = name;
    }

    public static AccountHolder from(AccountHolderRole role, String name) {
        return new AccountHolder(role, name);
    }
}
