package se.tink.libraries.payment.rpc;

import se.tink.libraries.account.rpc.Account;

public class Creditor {
    private Account account;

    public Creditor(Account account) {
        this.account = account;
    }

    public Account getAccount() {
        return account;
    }
}
