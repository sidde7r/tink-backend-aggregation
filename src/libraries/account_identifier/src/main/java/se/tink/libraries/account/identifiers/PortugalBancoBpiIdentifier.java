package se.tink.libraries.account.identifiers;

import se.tink.libraries.account.AccountIdentifier;

public class PortugalBancoBpiIdentifier extends AccountIdentifier {

    private String identifier;

    public PortugalBancoBpiIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public boolean isValid() {
        return identifier.length() == 13;
    }

    @Override
    public Type getType() {
        return Type.PT_BPI;
    }
}
