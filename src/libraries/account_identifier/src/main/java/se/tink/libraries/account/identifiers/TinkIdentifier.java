package se.tink.libraries.account.identifiers;

import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.uuid.UUIDUtils;

public class TinkIdentifier extends AccountIdentifier {

    private String identifier;

    private final boolean isValid;

    public TinkIdentifier(final String identifier) {
        this.identifier = identifier;
        isValid = UUIDUtils.isValidUUIDv4(identifier);
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public Type getType() {
        return Type.TINK;
    }

    @Override
    public boolean isValid() {
        return isValid;
    }
}
