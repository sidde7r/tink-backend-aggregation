package se.tink.libraries.identitydata.countries;

import com.google.common.base.Strings;

public enum EsIdentityDocumentType {
    NIF,
    NIE,
    OTHER;

    private static final String NIF_PATTERN = "\\d{8}[A-Z]";
    private static final String NIE_PATTERN = "[X-Z]\\d{7}[A-Z]";

    public static EsIdentityDocumentType typeOf(String identifier) {
        if (Strings.isNullOrEmpty(identifier)) {
            return OTHER;
        } else if (isValidNif(identifier)) {
            return NIF;
        } else if (isValidNie(identifier)) {
            return NIE;
        } else {
            return OTHER;
        }
    }

    public static boolean isValidNie(String identifier) {
        identifier = trimDni(identifier);
        return identifier.matches(NIE_PATTERN) && validNieChecksumLetter(identifier);
    }

    private static boolean validNieChecksumLetter(String input) {
        char initial = input.charAt(0);
        int value = getNieInitialValue(initial);

        return isValidNif(value + input.substring(1));
    }

    public static boolean isValidNif(String identifier) {
        identifier = trimDni(identifier);
        return identifier.matches(NIF_PATTERN) && validNifChecksumLetter(identifier);
    }

    public static String trimDni(String identifier) {
        identifier = identifier.trim().replace("-", "");

        if (identifier.charAt(0) == '0' && identifier.length() == 10) {
            identifier = identifier.substring(1);
        }

        return identifier;
    }

    private static boolean validNifChecksumLetter(String input) {
        // This won't throw NumberFormatException since we just checked the input format with regex
        int nifNumber = Integer.parseInt(input.substring(0, 8));
        char nifChecksumLetter = input.charAt(8);
        int remainder = nifNumber % 23;

        return validChecksumLetter(remainder, nifChecksumLetter);
    }

    private static boolean validChecksumLetter(int remainder, char letter) {
        return getChecksumLetter(remainder) == letter;
    }

    private static char getChecksumLetter(int remainder) {
        switch (remainder) {
            case 0:
                return 'T';
            case 1:
                return 'R';
            case 2:
                return 'W';
            case 3:
                return 'A';
            case 4:
                return 'G';
            case 5:
                return 'M';
            case 6:
                return 'Y';
            case 7:
                return 'F';
            case 8:
                return 'P';
            case 9:
                return 'D';
            case 10:
                return 'X';
            case 11:
                return 'B';
            case 12:
                return 'N';
            case 13:
                return 'J';
            case 14:
                return 'Z';
            case 15:
                return 'S';
            case 16:
                return 'Q';
            case 17:
                return 'V';
            case 18:
                return 'H';
            case 19:
                return 'L';
            case 20:
                return 'C';
            case 21:
                return 'K';
            case 22:
                return 'E';
            default:
                return '-';
        }
    }

    private static char getNieInitialValue(char letter) {
        switch (letter) {
            case 'X':
                return 0;
            case 'Y':
                return 1;
            case 'Z':
                return 2;
            default:
                throw new IllegalArgumentException("NIE cannot start with anything but X/Y/Z");
        }
    }
}
