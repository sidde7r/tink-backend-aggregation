package se.tink.libraries.account.identifiers;

import se.tink.libraries.account.AccountIdentifier;

public class NonValidIdentifier extends AccountIdentifier {

    private String identifier;

    public NonValidIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String getIdentifier() {
        return this.identifier;
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public Type getType() {
        return null;
    }
}
