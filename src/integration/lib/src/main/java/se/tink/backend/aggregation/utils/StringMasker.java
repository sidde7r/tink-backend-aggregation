package se.tink.backend.aggregation.utils;

import com.google.common.base.Strings;

public class StringMasker {

    private static final String STAR = "*";
    private static final int SHOWN_CHARS = 4;

    /**
     * Masks strings to avoid showing sensitive data. For example, "ABC12345678909876543" would
     * become "***********************6543".
     */
    public static String mask(String unmasked) {
        if (Strings.isNullOrEmpty(unmasked)) {
            return unmasked;
        }
        if (unmasked.length() <= SHOWN_CHARS * 2) {
            return generateReplacement(unmasked.length());
        }

        int lastStar = unmasked.length() - SHOWN_CHARS;
        return generateReplacement(lastStar) + unmasked.substring(lastStar);
    }

    private static String generateReplacement(int length) {
        return new String(new char[length]).replace("\0", STAR);
    }
}
