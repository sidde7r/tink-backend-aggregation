package se.tink.libraries.account.identifiers;

import com.google.common.base.Preconditions;
import java.util.regex.Pattern;
import se.tink.libraries.account.AccountIdentifier;

public class SortCodeIdentifier extends AccountIdentifier {

    // The first 6 digits in the pattern below are branch sort code while the remaining 7-8 digits
    // are account number.
    private static final Pattern SORT_CODE_AND_ACCOUNT_NUMBER =
            Pattern.compile("(\\d{6})([\\d]{7,8})");
    private static final int SORT_CODE_LENGTH = 6;

    private final String sortCode;
    private final String accountNumber;
    private final boolean isValid;

    public SortCodeIdentifier(final String identifier) {
        Preconditions.checkArgument(identifier != null, "Account identifier can not be null");

        String numericSortCodeAccountNumber = getNumericNumber(identifier);
        isValid = SORT_CODE_AND_ACCOUNT_NUMBER.matcher(numericSortCodeAccountNumber).matches();
        if (!isValid) {
            throw new IllegalArgumentException(
                    String.format("%s is not a valid account identifier", identifier));
        }
        sortCode = numericSortCodeAccountNumber.substring(0, SORT_CODE_LENGTH);
        accountNumber = numericSortCodeAccountNumber.substring(SORT_CODE_LENGTH);
    }

    private String getNumericNumber(String s) {
        return s.replaceAll("[^0-9]", "");
    }

    /**
     * Pad 7 digit account numbers with 0 so that all identifiers have lenght 14. (6 digit sort code
     * and 8 digit account number).
     */
    @Override
    public String getIdentifier() {
        return sortCode + (accountNumber.length() == 7 ? "0" : "") + accountNumber;
    }

    @Override
    public boolean isValid() {
        return isValid;
    }

    @Override
    public Type getType() {
        return Type.SORT_CODE;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getSortCode() {
        return sortCode;
    }
}
