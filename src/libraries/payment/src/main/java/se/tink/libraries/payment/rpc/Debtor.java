package se.tink.libraries.payment.rpc;

import se.tink.libraries.account.rpc.Account;

public class Debtor {
    private Account account;

    public Debtor(Account account) {
        this.account = account;
    }

    public Account getAccount() {
        return account;
    }
}
