package se.tink.libraries.account.identifiers;

import java.util.Collections;

public class BelgianIdentifier extends IbanIdentifier {

    public BelgianIdentifier(final String iban) {
        super(null, iban, Collections.singletonList(Type.BE.toString()));
    }

    @Override
    public Type getType() {
        return Type.BE;
    }
}
