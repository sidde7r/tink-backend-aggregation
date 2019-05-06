package se.tink.libraries.identitydata.countries;

import com.google.common.base.Preconditions;
import java.time.LocalDate;
import java.util.Map;
import se.tink.libraries.identitydata.IdentityData;

public class SeIdentityData extends IdentityData {

    private final String ssn;

    private static final String SSN_PATTERN = "(19|20)\\d{2}(0[1-9]|1[0-2])([0-2]\\d|3[0-1])\\d{4}";

    private SeIdentityData(Builder builder) {
        super(builder);
        this.ssn = builder.ssn;
    }

    public static IdentityData of(String fullName, String ssn) {
        return builder()
                .setSsn(ssn)
                .setFullName(fullName)
                .setDateOfBirth(getBirthDate(ssn))
                .build();
    }

    public static IdentityData of(String firstName, String lastName, String ssn) {
        return builder()
                .setSsn(ssn)
                .addFirstNameElement(firstName)
                .addSurnameElement(lastName)
                .setDateOfBirth(getBirthDate(ssn))
                .build();
    }

    static String processSsn(String ssn) {
        Preconditions.checkNotNull(ssn, "SSN must not be null");

        String trimmedSsn = ssn.replaceAll("[^\\d]", "");
        Preconditions.checkState(
                trimmedSsn.length() == 12, "SSN of invalid length %s", trimmedSsn.length());

        Preconditions.checkState(trimmedSsn.matches(SSN_PATTERN), "Invalid SSN");

        // SSN is valid
        return trimmedSsn;
    }

    public static LocalDate getBirthDate(String ssn) {
        final String processedSsn = processSsn(ssn);

        int year = Integer.parseInt(processedSsn.substring(0, 4));
        int month = Integer.parseInt(processedSsn.substring(5, 6));
        int day = Integer.parseInt(processedSsn.substring(7, 8));

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
