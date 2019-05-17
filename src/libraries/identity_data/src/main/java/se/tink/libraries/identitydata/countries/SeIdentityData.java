package se.tink.libraries.identitydata.countries;

import com.google.common.base.Preconditions;
import java.time.LocalDate;
import java.time.Period;
import java.util.Map;
import se.tink.libraries.identitydata.IdentityData;

public class SeIdentityData extends IdentityData {

    private final String ssn;

    private static final String SSN_PATTERN = "(19|20)\\d{2}(0[1-9]|1[0-2])([0-2]\\d|3[0-1])\\d{4}";

    private SeIdentityData(Builder builder) {
        super(builder);
        this.ssn = builder.ssn;
    }

    /**
     * Create a Swedish Identity Data object from name and SSN ("personnummer")
     *
     * <p>Date of birth is extracted from the SSN. The format of the SSN is validated, but the
     * number itself is not validated.
     *
     * @param fullName First and last name
     * @param ssn SSN (personnummer) with or without dashes/whitespace/etc, of length 10 or 12
     * @return Swedish Identity Data object with given name, reformatted SSN and date of birth
     */
    public static IdentityData of(String fullName, String ssn) {
        return builder()
                .setSsn(ssn)
                .setFullName(fullName)
                .setDateOfBirth(getBirthDateFromSsn(ssn))
                .build();
    }

    /**
     * Create a Swedish Identity Data object from name and SSN ("personnummer")
     *
     * <p>Date of birth is extracted from the SSN. The format of the SSN is validated, but the
     * number itself is not validated.
     *
     * @param firstName First name
     * @param surName Last name
     * @param ssn SSN (personnummer) with or without dashes/whitespace/etc, of length 10 or 12
     * @return Swedish Identity Data object with given name, reformatted SSN and date of birth
     */
    public static IdentityData of(String firstName, String surName, String ssn) {
        return builder()
                .setSsn(ssn)
                .addFirstNameElement(firstName)
                .addSurnameElement(surName)
                .setDateOfBirth(getBirthDateFromSsn(ssn))
                .build();
    }

    static String processSsn(String ssn) {
        Preconditions.checkNotNull(ssn, "SSN must not be null");

        String trimmedSsn = ssn.replaceAll("[^\\d]", "");

        // For all 10 digit SSNs we extend to 12 digits
        if (trimmedSsn.length() == 10) {
            if (ssn.trim().contains("+")) {
                // Age 100+ according to the Swedish SSN specification
                trimmedSsn = "19" + trimmedSsn;
            } else {
                // Extend short SSNs to include all 12 digits
                trimmedSsn = extendSsn(trimmedSsn);
            }
        }

        Preconditions.checkArgument(
                trimmedSsn.length() == 12, "SSN of invalid length %s", trimmedSsn.length());

        Preconditions.checkArgument(trimmedSsn.matches(SSN_PATTERN), "Invalid SSN");

        // SSN has valid format
        return trimmedSsn;
    }

    /**
     * Extend a 10-digit Swedish SSN to 12 digits. We do this by making the following check:
     *
     * <p>Given today's date, we look at how old the customer with the current SSN would be
     * <strong>today</strong> if we added "19" to the beginning of their SSN.
     *
     * <ul>
     *   <li>If that makes them at most 112 years old, we assume they were born in the 1900s
     *   <li>If that makes them 113 or older, we instead assume they are born in the 2000s
     * </ul>
     *
     * @param ssn 10 character long SSN String
     * @return 12 character SSN for a customer of age 13-112
     */
    static String extendSsn(String ssn) {
        Preconditions.checkArgument(ssn.length() == 10, "Invalid length sent to extension");
        LocalDate assumedBirthDate = getBirthDateFromSsn("19" + ssn);
        int age = Period.between(assumedBirthDate, LocalDate.now()).getYears();

        return age < 113 ? "19" + ssn : "20" + ssn;
    }

    public static LocalDate getBirthDateFromSsn(String ssn) {
        final String processedSsn = processSsn(ssn);

        int year = Integer.parseInt(processedSsn.substring(0, 4));
        int month = Integer.parseInt(processedSsn.substring(4, 6));
        int day = Integer.parseInt(processedSsn.substring(6, 8));

        return LocalDate.of(year, month, day);
    }

    public static SeIdentityDataBuilder builder() {
        return new Builder();
    }

    public interface SeIdentityDataBuilder extends IdentityData.InitialBuilderStep {
        InitialBuilderStep setSsn(String val);
    }

    protected static final class Builder extends IdentityData.Builder
            implements SeIdentityDataBuilder {
        private String ssn;

        protected Builder() {}

        /**
         * @param ssn SSN (personnummer) with or without dashes/whitespace/etc, of length 10 or 12
         * @return The next builder step.
         */
        @Override
        public SeIdentityDataBuilder setSsn(String ssn) {
            this.ssn = processSsn(ssn);
            return this;
        }

        public SeIdentityData build() {
            return new SeIdentityData(this);
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
}
