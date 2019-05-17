package se.tink.libraries.identitydata.countries;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.time.LocalDate;
import java.util.Map;
import se.tink.libraries.identitydata.IdentityData;

public class FiIdentityData extends IdentityData {

    private final String ssn;

    private static final String SSN_PATTERN =
            "([0-2]\\d|3[0-1])(0[1-9]|1[0-2])\\d{2}[+-A]\\d{3}[0-9A-FHJ-NPR-Y]";

    private FiIdentityData(Builder builder) {
        super(builder);
        this.ssn = builder.ssn;
    }

    /**
     * Create a Finnish Identity Data object from name and SSN ("henkilötunnus")
     *
     * <p>Date of birth is extracted from the SSN. The SSN is validated by checking:
     *
     * <ol>
     *   <li>that its format is correct
     *   <li>that the birth date is an actual date (date range is not checked)
     *   <li>that the control character (checksum) is valid for the given SSN
     * </ol>
     *
     * @param fullName First and last name
     * @param ssn SSN (henkilötunnus) with or without surrounding whitespace, of length 11
     * @return Finnish Identity Data object with given name, reformatted SSN and date of birth
     */
    public static IdentityData of(String fullName, String ssn) {
        return builder()
                .setSsn(ssn)
                .setFullName(fullName)
                .setDateOfBirth(getBirthDateFromSsn(ssn))
                .build();
    }

    /**
     * Create a Finnish Identity Data object from name and SSN ("henkilötunnus")
     *
     * <p>Date of birth is extracted from the SSN. The SSN is validated by checking:
     *
     * <ol>
     *   <li>that its format is correct
     *   <li>that the birth date is an actual date (date range is not checked)
     *   <li>that the control character (checksum) is valid for the given SSN
     * </ol>
     *
     * @param firstName First name
     * @param surName Last name
     * @param ssn SSN (henkilötunnus) with or without surrounding whitespace, of length 11
     * @return Finnish Identity Data object with given name, reformatted SSN and date of birth
     */
    public static IdentityData of(String firstName, String surName, String ssn) {
        return builder()
                .setSsn(ssn)
                .addFirstNameElement(firstName)
                .addSurnameElement(surName)
                .setDateOfBirth(getBirthDateFromSsn(ssn))
                .build();
    }

    public static FiIdentityDataBuilder builder() {
        return new Builder();
    }

    public interface FiIdentityDataBuilder extends IdentityData.InitialBuilderStep {
        InitialBuilderStep setSsn(String val);
    }

    protected static final class Builder extends IdentityData.Builder
            implements FiIdentityDataBuilder {
        private String ssn;

        protected Builder() {}

        /**
         * @param ssn SSN (henkilötunnus) with or without surrounding whitespace, of length 11
         * @return The next builder step.
         */
        @Override
        public FiIdentityDataBuilder setSsn(String ssn) {
            this.ssn = processSsn(ssn);
            return this;
        }

        public FiIdentityData build() {
            return new FiIdentityData(this);
        }
    }

    @Override
    public Map<String, String> toMap() {
        Map<String, String> map = baseMap();
        if (ssn != null) {
            map.put("ssn", ssn);
        }

        return map;
    }

    @Override
    public String getSsn() {
        return ssn;
    }

    static String processSsn(String ssn) {
        if (Strings.isNullOrEmpty(ssn)) {
            throw new IllegalArgumentException("SSN must not be null or empty.");
        }

        final String preProcessedSsn = ssn.trim().toUpperCase();
        final String trimmedSsn = preProcessedSsn.replaceAll("[^\\dA-Z+-]", "");

        Preconditions.checkArgument(isValidSsn(trimmedSsn), "Not a valid SSN");

        // SSN has matching format after pre-processing (this does not mean it is validated)
        return trimmedSsn;
    }

    private static boolean isValidSsn(String trimmedSsn) {
        Preconditions.checkArgument(
                trimmedSsn.length() == 11, "SSN of invalid length: %s", trimmedSsn.length());

        Preconditions.checkArgument(trimmedSsn.matches(SSN_PATTERN), "SSN has invalid format");

        // Check control character
        int digits = Integer.parseInt(trimmedSsn.substring(0, 6) + trimmedSsn.substring(7, 10));
        char checksumChar = trimmedSsn.substring(10, 11).charAt(0);

        return isValidControlChar(digits, checksumChar);
    }

    public static LocalDate getBirthDateFromSsn(String ssn) {
        final String trimmedSsn = processSsn(ssn);
        Preconditions.checkArgument(trimmedSsn.matches(SSN_PATTERN), "SSN has invalid format");

        int day = Integer.parseInt(trimmedSsn.substring(0, 2));
        int month = Integer.parseInt(trimmedSsn.substring(2, 4));
        int yy = Integer.parseInt(trimmedSsn.substring(4, 6));
        char centuryChar = trimmedSsn.substring(6, 7).charAt(0);

        int year = getCenturyFromCenturyChar(centuryChar) * 100 + yy;

        return LocalDate.of(year, month, day);
    }

    private static int getCenturyFromCenturyChar(char centuryChar) {
        switch (centuryChar) {
            case '+':
                return 18;
            case '-':
                return 19;
            case 'A':
                return 20;
            default:
                throw new IllegalArgumentException("Invalid century character: " + centuryChar);
        }
    }

    private static boolean isValidControlChar(int num, char controlChar) {
        int actualRemainder = num % 31;
        int expectedRemainder = getRemainderFromControlChar(controlChar);

        return actualRemainder == expectedRemainder;
    }

    private static int getRemainderFromControlChar(char controlChar) {
        switch (controlChar) {
            case '0':
                return 0;
            case '1':
                return 1;
            case '2':
                return 2;
            case '3':
                return 3;
            case '4':
                return 4;
            case '5':
                return 5;
            case '6':
                return 6;
            case '7':
                return 7;
            case '8':
                return 8;
            case '9':
                return 9;
            case 'A':
                return 10;
            case 'B':
                return 11;
            case 'C':
                return 12;
            case 'D':
                return 13;
            case 'E':
                return 14;
            case 'F':
                return 15;
            case 'H':
                return 16;
            case 'J':
                return 17;
            case 'K':
                return 18;
            case 'L':
                return 19;
            case 'M':
                return 20;
            case 'N':
                return 21;
            case 'P':
                return 22;
            case 'R':
                return 23;
            case 'S':
                return 24;
            case 'T':
                return 25;
            case 'U':
                return 26;
            case 'V':
                return 27;
            case 'W':
                return 28;
            case 'X':
                return 29;
            case 'Y':
                return 30;
            default:
                throw new IllegalArgumentException("Invalid control character: " + controlChar);
        }
    }
}
