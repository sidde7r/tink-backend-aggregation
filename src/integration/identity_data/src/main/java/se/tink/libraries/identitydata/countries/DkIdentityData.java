package se.tink.libraries.identitydata.countries;

import java.time.LocalDate;
import se.tink.libraries.identitydata.IdentityData;

public class DkIdentityData extends IdentityData {

    public DkIdentityData(Builder builder) {
        super(builder);
    }

    /**
     * Create a Danish Identity Data object from name and CPR
     *
     * <p>Date of birth is extracted from the CPR.
     *
     * @param fullName First and last name
     * @param cpr Personal identification number of length 10
     * @return Danish Identity Data object with given name, CPR, and date of birth
     */
    public static IdentityData of(String fullName, String cpr) {
        return builder()
                .setSsn(cpr)
                .setFullName(fullName)
                .setDateOfBirth(getBirthDateFromCpr(cpr))
                .build();
    }

    /**
     * Create a Danish Identity Data object from name and CPR
     *
     * <p>Date of birth is extracted from the CPR.
     *
     * @param firstName First name
     * @param surname Last name
     * @param cpr Personal identification number of length 10
     * @return Danish Identity Data object with given name, CPR, and date of birth
     */
    public static IdentityData of(String firstName, String surname, String cpr) {
        return builder()
                .setSsn(cpr)
                .addFirstNameElement(firstName)
                .addSurnameElement(surname)
                .setDateOfBirth(getBirthDateFromCpr(cpr))
                .build();
    }

    private static LocalDate getBirthDateFromCpr(String cpr) {
        final int day = Integer.parseInt(cpr.substring(0, 2));
        final int month = Integer.parseInt(cpr.substring(2, 4));
        final int shortYear = Integer.parseInt(cpr.substring(4, 6));
        final int yearCenturyPart = Integer.parseInt(cpr.substring(6, 7));

        final int year = calculateYear(shortYear, yearCenturyPart);

        return LocalDate.of(year, month, day);
    }

    // https://da.wikipedia.org/wiki/CPR-nummer#Under_eller_over_100_%C3%A5r
    private static int calculateYear(int shortYear, int yearCenturyPart) {
        int century;
        if (yearCenturyPart >= 0 && yearCenturyPart <= 3) {
            century = 1900;
        } else if (yearCenturyPart == 4 || yearCenturyPart == 9) {
            if (shortYear >= 0 && shortYear <= 36) {
                century = 2000;
            } else {
                century = 1900;
            }
        } else {
            century = 2000;
        }
        return century + shortYear;
    }
}
