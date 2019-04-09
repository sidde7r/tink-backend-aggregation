package se.tink.libraries.account.identifiers;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.se.ClearingNumber;
import se.tink.libraries.account.identifiers.se.ClearingNumber.Bank;

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
}
