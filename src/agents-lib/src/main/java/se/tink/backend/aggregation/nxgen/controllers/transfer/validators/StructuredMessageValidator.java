package se.tink.backend.aggregation.nxgen.controllers.transfer.validators;

import com.google.common.base.Strings;
import se.tink.backend.utils.StringUtils;

public class StructuredMessageValidator {

    /**
     * In Belgium, the national standard “OGM-VCS” is widely used for structured remittance information during decades.
     * The format:
     *  - Electronic: 12 digits (example: 010806817183);
     *  - Visual: +++ 3 digits / 4 digits / 5 digits / +++ (example: +++010/8068/17183+++)
     *  - Check digits: the last 2 digits as check digits (modulo 97) of the first 10 digits, but if the result is 0,
     *    then the check digits are 97.
     */
    public static boolean isValidOgmVcs(String reference) {
        if (Strings.isNullOrEmpty(reference)) {
            return false;
        }

        reference = formatStructuredReference(reference);

        if (!StringUtils.isNumeric(reference) || reference.length() != 12) {
            return false;
        }

        long generatorDigits = Long.parseLong(reference.substring(0, 10));
        long checkDigits = Long.parseLong(reference.substring(10, 12));

        return (generatorDigits % 97 == 0 && checkDigits == 97)
                || generatorDigits % 97 == checkDigits;
    }

    private static String formatStructuredReference(String reference) {
        return reference.replaceAll("[+/]", "").trim();
    }
}
