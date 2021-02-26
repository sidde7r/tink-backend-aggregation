package se.tink.libraries.account.identifiers;

import com.google.common.base.Strings;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.iban4j.Iban.Builder;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.se.ClearingNumber;
import se.tink.libraries.account.identifiers.se.ClearingNumber.Bank;
import se.tink.libraries.account.identifiers.se.IbanCodes;

public class SwedishIdentifier extends AccountIdentifier {

    private String clearingNumber;
    private String accountNumber;
    private String bankName;

    private final boolean isValid;
    private Bank bank;

    // The 6 or longer for the accountnumber (without clearing) is arbitarily taken.
    // Might need to lower to 5 or longer?
    private static final Pattern PATTERN_ACCOUNT_NUMBER = Pattern.compile("(\\d{4})([\\d]{6,})");

    public SwedishIdentifier(final String identifier) {
        isValid = formatAccountNumber(identifier);
    }

    private boolean formatAccountNumber(String fullAccountNumber) {
        if (fullAccountNumber == null) {
            fullAccountNumber = "";
        }

        // TODO Understand if an accountnumber can start with zeroes ?
        // TODO If so, handle removal of those

        String clean =
                fullAccountNumber
                        .replace("-", "")
                        .replace(" ", "")
                        .replace(",", "")
                        .replace(".", "");
        Matcher m = PATTERN_ACCOUNT_NUMBER.matcher(clean);
        if (m.matches()) {
            Optional<ClearingNumber.Details> details = ClearingNumber.get(m.group(1));

            if (!details.isPresent()) {
                return false;
            }

            ClearingNumber.Details detailsValue = details.get();

            bankName = detailsValue.getBankName();
            bank = detailsValue.getBank();
            clearingNumber = clean.substring(0, detailsValue.getClearingNumberLength());
            accountNumber = clean.substring(detailsValue.getClearingNumberLength());

            return true;
        }
        return false;
    }

    public Bank getBank() {
        return bank;
    }

    public String getBankName() {
        return bankName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getClearingNumber() {
        return clearingNumber;
    }

    @Override
    public String getIdentifier() {
        return clearingNumber + accountNumber;
    }

    @Override
    public AccountIdentifier.Type getType() {
        return Type.SE;
    }

    @Override
    public boolean isValid() {
        return isValid;
    }

    /**
     * toIbanIdentifer() reads the bankCode from IbanCodes which takes the Bank object as a key.
     * Currently only works for SE since bankCode only contains banks in Sweden and that
     * ACCOUNT_NUMBER_PADDING_LENGTH is hardcoded for SE.
     *
     * @return IbanIdentifer with the created Iban number.
     */
    public IbanIdentifier toIbanIdentifer() {
        Integer bankCode = IbanCodes.getBankCode(bank);
        String paddedAccountNumber = padAccountNumber();
        Iban iban =
                new Builder()
                        .countryCode(CountryCode.SE)
                        .bankCode(bankCode.toString())
                        .accountNumber(paddedAccountNumber)
                        .build();
        return new IbanIdentifier(iban.toString());
    }

    /**
     * @return the padded accountNumber of length ACCOUNT_NUMBER_PADDING_LENGTH (which is 17 for SE)
     *     left padded with '0'.
     */
    private String padAccountNumber() {
        return Strings.padStart(
                clearingNumber + accountNumber,
                IbanCodes.ACCOUNT_NUMBER_PADDING_LENGTH,
                IbanCodes.PADDING_CHAR);
    }
}
