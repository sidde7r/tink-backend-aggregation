package se.tink.libraries.account.identifiers;

import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.social.security.SocialSecurityNumber;

public class NDAPersonalNumberIdentifier extends AccountIdentifier {

    private static final String CLEARING_NUMBER = "3300";
    private String accountNumber;
    private final boolean isValid;

    public NDAPersonalNumberIdentifier(final String identifier) {
        isValid = formatAccountNumber(identifier);
    }

    private boolean formatAccountNumber(String personalNumber) {
        if (personalNumber == null) {
            personalNumber = "";
        }

        String clean =
                personalNumber.replace("-", "").replace(" ", "").replace(",", "").replace(".", "");
        SocialSecurityNumber.Sweden ssn = new SocialSecurityNumber.Sweden(clean);

        if (!ssn.isValid()) {
            return false;
        }
        accountNumber = clean;
        return true;
    }

    @Override
    public String getIdentifier() {
        return accountNumber;
    }

    @Override
    public Type getType() {
        return Type.SE_NDA_SSN;
    }

    @Override
    public boolean isValid() {
        return isValid;
    }

    public SwedishIdentifier toSwedishIdentifier() {
        return new SwedishIdentifier(CLEARING_NUMBER + accountNumber);
    }
}
