package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi;

import com.google.common.base.Preconditions;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IBANPortugal {

    private static int[] NIB_WEIGHTS =
            new int[] {73, 17, 89, 38, 62, 45, 53, 15, 50, 5, 49, 34, 81, 76, 27, 90, 9, 30, 3};
    private static int NIB_WHICH_OUT_CHECK_DIGITS_LENGTH = 19;
    private static final int MOD = 97;
    private static final Pattern NIB_WHICH_OUT_CHECK_DIGITS_PATTERN =
            Pattern.compile("^[0-9]{19}$");
    private static final Pattern FOUR_DIGITS_PATTERN = Pattern.compile("^[0-9]{4}$");
    private static final String PORTUGAL_COUNTRY_CODE = "PT";
    private static final String PORTUGAL_IBAN_CHECK_DIGITS = "50";

    public static String calculateNIBCheckDigits(String nibWhichOutCheckDigits) {
        Preconditions.checkArgument(
                NIB_WHICH_OUT_CHECK_DIGITS_PATTERN.matcher(nibWhichOutCheckDigits).matches(),
                "NIB which out check digits has to be 19-digits length");
        long s = 0;
        for (int i = 0; i < NIB_WHICH_OUT_CHECK_DIGITS_LENGTH; i++) {
            s += NIB_WEIGHTS[i] * (nibWhichOutCheckDigits.charAt(i) - 48);
        }
        s = 98 - s % MOD;
        String checkDigits = "" + s;
        return checkDigits.length() == 1 ? "0" + checkDigits : checkDigits;
    }

    public static String generateIBAN(
            final String bankId, final String pspRefNumber, final String accountNo) {
        Preconditions.checkArgument(
                FOUR_DIGITS_PATTERN.matcher(bankId).matches(), "Bank id has to be 4-digits length");
        Preconditions.checkArgument(
                FOUR_DIGITS_PATTERN.matcher(pspRefNumber).matches(),
                "PSP reference number has to be 4-digits length");
        String NIB = bankId + pspRefNumber + accountNo;
        if (NIB.length() == NIB_WHICH_OUT_CHECK_DIGITS_LENGTH) {
            NIB += calculateNIBCheckDigits(NIB);
        }
        return PORTUGAL_COUNTRY_CODE + PORTUGAL_IBAN_CHECK_DIGITS + " " + NIB;
    }
}
