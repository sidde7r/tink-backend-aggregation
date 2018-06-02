package se.tink.libraries.account.identifiers;

import com.google.common.base.Strings;
import org.iban4j.BicFormatException;
import org.iban4j.BicUtil;
import org.iban4j.IbanFormatException;
import org.iban4j.IbanUtil;
import org.iban4j.InvalidCheckDigitException;
import org.iban4j.UnsupportedCountryException;
import se.tink.libraries.account.AccountIdentifier;

public class IbanIdentifier extends AccountIdentifier {

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

    public IbanIdentifier(Type type, String bic, String iban) {
        if (validateBicAndIbanWithType(type, bic, iban)) {
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

    private boolean validateBicAndIbanWithType(Type type, String bic, String iban) {
        boolean isValidBicAndIban = validateBicAndIban(bic, iban);
        String countryCode = IbanUtil.getCountryCode(iban);

        return isValidBicAndIban && type.toString().equalsIgnoreCase(countryCode);
    }

    private boolean validateIban(String iban) {
        try {
            IbanUtil.validate(iban);
            return true;
        } catch (IbanFormatException | InvalidCheckDigitException | UnsupportedCountryException ignored) {
            return false;
        }
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

}
