package se.tink.libraries.account.identifiers.se.swedbank;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.formatters.DefaultAccountIdentifierFormatter;
import se.tink.libraries.account.identifiers.se.ClearingNumber;
import se.tink.libraries.giro.validation.LuhnCheck;

public class SwedbankClearingNumberUtils {
    private static final DefaultAccountIdentifierFormatter DEFAULT_FORMATTER =
            new DefaultAccountIdentifierFormatter();

    /**
     * For appending the missing fifth clearing digit to a four digit clearing. (Not to be used with
     * a complete account identifier. For that, use the insertFifthClearingDigit method instead.)
     */
    public static String appendFifthClearingDigit(String fourDigitClearing) {
        Preconditions.checkArgument(
                StringUtils.isNumeric(fourDigitClearing), "Not a number string");
        Preconditions.checkArgument(
                Objects.equal(fourDigitClearing.length(), 4), "Not a four digit clearingnumber");
        Preconditions.checkArgument(
                fourDigitClearing.startsWith("8"), "Not a Swedbank 8xxx clearing number");

        return fourDigitClearing + LuhnCheck.calculateLuhnMod10Check(fourDigitClearing);
    }

    /**
     * For inserting the missing fifth clearing digit into an account identifier. It does this by
     * using the calculated Luhn digit and inserting it at position 5 in the given string.
     */
    public static String insertFifthClearingDigit(String accountIdentifier) {
        Preconditions.checkArgument(
                StringUtils.isNumeric(accountIdentifier), "Not a number string");
        Preconditions.checkArgument(
                accountIdentifier.length() >= 5, "Less than five digit account identifier");
        Preconditions.checkArgument(
                accountIdentifier.startsWith("8"), "Not a Swedbank 8xxx clearing number");

        String calculatedFiveDigitClearing =
                appendFifthClearingDigit(accountIdentifier.substring(0, 4));
        String accountDigits = accountIdentifier.substring(4, accountIdentifier.length());

        return calculatedFiveDigitClearing + accountDigits;
    }

    /**
     * This checks if the fifth character in the given 8CCC[C|N]NNNNNNN account identifier String is
     * a check digit, in which case it could be the fifth clearing digit in a Swedbank 8xxx account
     * identifier.
     */
    public static boolean isFifthDigitValidCheck(String accountIdentifier) {
        Preconditions.checkArgument(
                StringUtils.isNumeric(accountIdentifier), "Not a number string");
        Preconditions.checkArgument(
                accountIdentifier.length() >= 5, "Less than five digit account identifier");
        Preconditions.checkArgument(
                accountIdentifier.startsWith("8"), "Not a Swedbank 8xxx account identifier");

        String firstFourDigits = accountIdentifier.substring(0, 4);
        String calculatedFiveDigitClearing = appendFifthClearingDigit(firstFourDigits);

        return accountIdentifier.startsWith(calculatedFiveDigitClearing);
    }

    /**
     * Pads a Swedbank identifier with missing zeros between clearing and account number until the
     * account identifier is 15 chars long.
     *
     * @param identifier 8xxxx… Account identifier (including fifth digit in clearing)
     *     <p>Example: 8422831270465 -> 842280031270465
     */
    public static String padWithZerosBetweenClearingAndAccountNumber(SwedishIdentifier identifier) {
        return padWithZerosBetweenClearingAndAccountNumber(
                identifier.getIdentifier(DEFAULT_FORMATTER));
    }

    /**
     * Pads a Swedbank account identifier with missing zeros between clearing and account number
     * until the account identifier is 15 chars long.
     *
     * @param identifier 8xxxx… Account identifier (including fifth digit in clearing)
     *     <p>Example: 8422831270465 -> 842280031270465
     */
    public static String padWithZerosBetweenClearingAndAccountNumber(String identifier) {
        Preconditions.checkArgument(StringUtils.isNumeric(identifier), "Not a number string");
        Preconditions.checkArgument(
                identifier.startsWith("8"), "Not a Swedbank 8xxx account identifier");
        Preconditions.checkArgument(
                isFifthDigitValidCheck(identifier), "Not a valid 5 digit clearing number");

        String clearing = identifier.substring(0, 5);
        String accountNumber = identifier.substring(5, identifier.length());
        return clearing + padWithZerosBeforeAccountNumber(accountNumber);
    }

    /**
     * Pads a Swedbank account number with missing zeros before account number until the account
     * number is 10 chars long.
     *
     * @param accountNumber Account number to be padded
     *     <p>Example: 31270465 -> 0031270465
     */
    public static String padWithZerosBeforeAccountNumber(String accountNumber) {
        return Strings.padStart(accountNumber, 10, '0');
    }

    public static boolean isSwedbank8xxxAccountNumber(AccountIdentifier identifier) {
        if (identifier.isValid() && identifier.is(AccountIdentifier.Type.SE)) {
            SwedishIdentifier swedishIdentifier = identifier.to(SwedishIdentifier.class);

            Optional<ClearingNumber.Details> clearingNumberDetails =
                    ClearingNumber.get(swedishIdentifier.getClearingNumber());

            if (clearingNumberDetails.isPresent()
                    && Objects.equal(
                            clearingNumberDetails.get().getBank(), ClearingNumber.Bank.SWEDBANK)) {
                if (swedishIdentifier.getClearingNumber().startsWith("8")) {
                    return true;
                }
            }
        }

        return false;
    }

    public static SwedishIdentifier removeZerosBetweenClearingAndAccountNumber(
            SwedishIdentifier identifier) {
        Preconditions.checkArgument(
                identifier.getClearingNumber().startsWith("8"),
                "Not a Swedbank 8xxx account identifier");

        String accountNumber = identifier.getAccountNumber();

        if (!accountNumber.startsWith("0") || accountNumber.length() <= 6) {
            return AccountIdentifier.create(
                            AccountIdentifier.Type.SE,
                            identifier.getIdentifier(DEFAULT_FORMATTER),
                            identifier.getName().orElse(null))
                    .to(SwedishIdentifier.class);
        }

        String cleanedAccountNumber = StringUtils.stripStart(accountNumber, "0");
        if (cleanedAccountNumber.length() <= 6) {
            // An account number cannot be less than six digits
            cleanedAccountNumber = Strings.padStart(cleanedAccountNumber, 6, '0');
        }

        return AccountIdentifier.create(
                        AccountIdentifier.Type.SE,
                        identifier.getClearingNumber() + cleanedAccountNumber,
                        identifier.getName().orElse(null))
                .to(SwedishIdentifier.class);
    }

    /**
     * Validates Swedbank account identifiers starting with 8xxx - Checks check digit of 5th
     * clearing digit - Checks check digit of last digit in account number
     */
    public static void validateIfSwedbank8xxxIdentifier(AccountIdentifier identifier)
            throws IllegalStateException {
        Preconditions.checkState(identifier.isValid());

        if (isSwedbank8xxxAccountNumber(identifier)) {
            SwedishIdentifier swedbankIdentifier = identifier.to(SwedishIdentifier.class);

            Preconditions.checkState(
                    isFifthDigitValidCheck(swedbankIdentifier.getIdentifier(DEFAULT_FORMATTER)));
            Preconditions.checkState(
                    isAccountNumberValidCheck(swedbankIdentifier.getAccountNumber()));
        }
    }

    private static boolean isAccountNumberValidCheck(String accountNumber) {
        return LuhnCheck.isLastCharCorrectLuhnMod10Check(accountNumber);
    }
}
