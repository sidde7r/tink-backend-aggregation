package se.tink.libraries.payment.rpc;

import se.tink.libraries.account.AccountIdentifier;

public class Debtor extends PaymentParty {

    public Debtor(AccountIdentifier accountIdentifier) {
        super(accountIdentifier);
    }
}
