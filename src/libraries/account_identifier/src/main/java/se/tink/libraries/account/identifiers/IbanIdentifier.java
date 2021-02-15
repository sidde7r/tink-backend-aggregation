package se.tink.libraries.account.identifiers;

import com.google.common.base.Strings;
import java.util.Collection;
import org.apache.commons.validator.routines.IBANValidator;
import org.iban4j.BicFormatException;
import org.iban4j.BicUtil;
import org.iban4j.IbanUtil;
import se.tink.libraries.account.AccountIdentifier;

public class IbanIdentifier extends AccountIdentifier {

    private static final IBANValidator IBAN_VALIDATOR = IBANValidator.getInstance();

    private final String iban;
    private final String bic;
    private boolean isValidBic;
    private boolean isValidIban;

    public IbanIdentifier(String identifier) {
        String[] parts = identifier.split("/");

        if (parts.length == 1 && validateIban(parts[0])) {
            this.bic = null;
            this.iban = parts[0];
            this.isValidIban = true;
        } else if (parts.length == 2 && validateBicAndIban(parts[0], parts[1])) {
            this.bic = parts[0];
            this.iban = parts[1];
            this.isValidBic = true;
            this.isValidIban = true;
        } else {
            this.bic = null;
            this.iban = null;
        }
    }

    public IbanIdentifier(String bic, String iban) {
        if (validateBicAndIban(bic, iban)) {
            this.bic = bic;
            this.iban = iban;
            this.isValidIban = true;
            this.isValidBic = !Strings.isNullOrEmpty(bic);
        } else {
            this.bic = null;
            this.iban = null;
        }
    }

    public IbanIdentifier(String bic, String iban, Collection<String> countryCodeRestrictions) {
        if (validateBicAndIbanWithCountryCodeRestriction(bic, iban, countryCodeRestrictions)) {
            this.bic = bic;
            this.iban = iban;
            this.isValidIban = true;
            this.isValidBic = !Strings.isNullOrEmpty(bic);
        } else {
            this.bic = null;
            this.iban = null;
        }
    }

    private boolean validateBicAndIban(String bic, String iban) {
        boolean isValidIban = validateIban(iban);
        return isValidIban && (Strings.isNullOrEmpty(bic) || validateBic(bic));
    }

    private boolean validateBicAndIbanWithCountryCodeRestriction(
            String bic, String iban, Collection<String> countryCodeRestrictions) {
        boolean isValidBicAndIban = validateBicAndIban(bic, iban);
        String countryCode = IbanUtil.getCountryCode(iban);

        return isValidBicAndIban && isValidCountry(countryCode, countryCodeRestrictions);
    }

    private boolean isValidCountry(String countryCode, Collection<String> countryCodeRestrictions) {
        return countryCodeRestrictions.stream()
                .anyMatch(country -> country.equalsIgnoreCase(countryCode));
    }

    private boolean validateIban(String iban) {
        return IBAN_VALIDATOR.isValid(iban);
    }

    private boolean validateBic(String bic) {
        try {
            BicUtil.validate(bic);
            return true;
        } catch (BicFormatException ignored) {
            return false;
        }
    }

    @Override
    public String getIdentifier() {
        if (Strings.isNullOrEmpty(bic)) {
            return iban;
        }

        return String.format("%s/%s", bic, iban);
    }

    @Override
    public boolean isValid() {
        if (Strings.isNullOrEmpty(bic)) {
            return isValidIban;
        }

        return isValidBic && isValidIban;
    }

    public String getBic() {
        return bic;
    }

    public String getIban() {
        return iban;
    }

    @Override
    public Type getType() {
        return Type.IBAN;
    }

    public boolean isValidIban() {
        return isValidIban;
    }

    public boolean isValidBic() {
        return isValidBic;
    }

    public String getBankCode() {
        return IbanUtil.getBankCode(iban);
    }

    public String getBban() {
        return IbanUtil.getBban(iban);
    }
}
