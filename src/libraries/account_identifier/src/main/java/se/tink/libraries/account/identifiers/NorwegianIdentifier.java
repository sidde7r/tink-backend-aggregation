package se.tink.libraries.account.identifiers;

import com.google.common.base.Strings;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.strings.StringUtils;

/**
 * Norwegian account number implemented according to specifications at:
 * https://docs.oracle.com/cd/E18727_01/doc.121/e13483/T359831T498954.htm
 */
public class NorwegianIdentifier extends AccountIdentifier {

    private static final int VALID_FULL_LENGTH = 11;
    private static final int SKIP_CHECK_RANGE_START = 4;
    private static final int SKIP_CHECK_RANGE_END = 6;
    private static final String SKIP_CHECK_SIGNATURE = "00";
    private static final int[] CHECK_FACTOR_TABLE = new int[] {5, 4, 3, 2, 7, 6, 5, 4, 3, 2};
    private static final int CHECK_DIVISOR = 11;

    private final String accountNumber;
    private final boolean isValid;

    public NorwegianIdentifier(String accountNumber) {
        this.accountNumber = accountNumber;
        isValid = checkValidity(accountNumber);
    }

    @Override
    public String getIdentifier() {
        return accountNumber;
    }

    @Override
    public boolean isValid() {
        return isValid;
    }

    @Override
    public Type getType() {
        return Type.NO;
    }

    private static boolean checkValidity(final String accountNumber) {

        // Pre-algorithm sanity checks.
        if (Strings.isNullOrEmpty(accountNumber)
                || accountNumber.length() != VALID_FULL_LENGTH
                || !StringUtils.isNumeric(accountNumber)) {
            return false;
        }

        // If account number is of a certain signature the check algorithm should not be applied.
        if (SKIP_CHECK_SIGNATURE.equalsIgnoreCase(
                accountNumber.substring(SKIP_CHECK_RANGE_START, SKIP_CHECK_RANGE_END))) {
            return true;
        }

        // Apply check algorithm.
        // Init total with check digit.
        int total = Character.getNumericValue(accountNumber.charAt(VALID_FULL_LENGTH - 1));

        // Sum factors of each remaining digit and the corresponding digit from the factor table.
        for (int i = 0; i < VALID_FULL_LENGTH - 1; i++) {
            total += Character.getNumericValue(accountNumber.charAt(i)) * CHECK_FACTOR_TABLE[i];
        }

        // Number if valid if remainder from division with CHECK_DIVISOR is 0.
        return Math.floorMod(total, CHECK_DIVISOR) == 0;
    }
}
