package se.tink.libraries.account.identifiers;

import se.tink.libraries.account.AccountIdentifier;

public class FinnishIdentifier extends AccountIdentifier {

    private String accountNumber;

    private final boolean isValid;

    public FinnishIdentifier(final String identifier) {
        accountNumber = identifier;
        isValid = true;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    @Override
    public String getIdentifier() {
        return accountNumber;
    }

    @Override
    public Type getType() {
        return Type.FI;
    }

    @Override
    public boolean isValid() {
        return isValid;
    }
}
