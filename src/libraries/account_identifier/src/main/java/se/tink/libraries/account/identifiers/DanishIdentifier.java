package se.tink.libraries.account.identifiers;

import se.tink.libraries.account.AccountIdentifier;

public class DanishIdentifier extends AccountIdentifier {

    private String accountNumber;

    public DanishIdentifier(String accountNumber) {
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
        return Type.DK;
    }
}
