package se.tink.libraries.account.identifiers;

import se.tink.libraries.account.AccountIdentifier;

public class UkIdentifier extends AccountIdentifier {

    public String accountNumber;

    public UkIdentifier(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    @Override
    public String getIdentifier() {
        return accountNumber;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public Type getType() {
        return Type.UK;
    }
}
