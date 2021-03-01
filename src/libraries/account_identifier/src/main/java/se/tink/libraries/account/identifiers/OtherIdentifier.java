package se.tink.libraries.account.identifiers;

import se.tink.libraries.account.AccountIdentifier;

/** Used when account identifier is not identified and can not be mapped to any other Identifier */
public class OtherIdentifier extends AccountIdentifier {
    private final String accountNumber;

    public OtherIdentifier(String accountNumber) {
        this.accountNumber = accountNumber;
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
        return Type.OTHER;
    }
}
