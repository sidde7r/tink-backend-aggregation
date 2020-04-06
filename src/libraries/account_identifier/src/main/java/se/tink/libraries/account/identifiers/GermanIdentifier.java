package se.tink.libraries.account.identifiers;

import se.tink.libraries.account.AccountIdentifier;

public class GermanIdentifier extends AccountIdentifier {

    private String blz;
    private String accountNumber;

    public GermanIdentifier(String blz, String accountNumber) {
        this.blz = blz;
        this.accountNumber = accountNumber;
    }

    public GermanIdentifier(String identifier) {
        String[] parts = identifier.split("/");
        blz = parts[0];
        accountNumber = parts[1];
    }

    @Override
    public String getIdentifier() {
        return String.format("%s/%s", blz, accountNumber);
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public Type getType() {
        return Type.DE;
    }

    public String getBlz() {
        return blz;
    }

    public String getAccountNumber() {
        return accountNumber;
    }
}
