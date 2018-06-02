package se.tink.libraries.account.identifiers;

public class BelgianIdentifier extends IbanIdentifier {

    public BelgianIdentifier(final String iban) {
        super(Type.BE, null, iban);
    }

    @Override
    public Type getType() {
        return Type.BE;
    }
}
