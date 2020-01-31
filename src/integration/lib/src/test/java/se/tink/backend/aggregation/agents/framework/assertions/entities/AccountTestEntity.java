package se.tink.backend.aggregation.agents.framework.assertions.entities;

import se.tink.backend.agents.rpc.Account;

public class AccountTestEntity implements Comparable<AccountTestEntity> {

    private Account account;

    public AccountTestEntity(Account account) {
        this.account = account;
    }

    @Override
    public boolean equals(Object o) {
        return Account.deepEquals(account, ((AccountTestEntity) o).account);
    }

    @Override
    public int compareTo(AccountTestEntity u) {
        return account.getAccountNumber().compareTo(u.account.getAccountNumber());
    }
}
