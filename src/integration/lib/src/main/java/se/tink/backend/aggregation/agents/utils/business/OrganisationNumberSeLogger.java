package se.tink.backend.aggregation.agents.utils.business;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;

/**
 * In Sweden we currently don't know which types of organisations we support. We know for sure that
 * we support Aktiebolag (limited company or corporation) and for some banks sole proprietorship.
 * For Aktiebolag the organisation number starts with 5, for sole proprietorship the organisation
 * number is a Swedish SSN. Unknown organisation numbers are defined as "not starting with 5 and not
 * an SSN". This class is used to log the first digit of the unknown organisation numbers for all
 * the banks we've implemented business aggregation for in Sweden.
 */
@Slf4j
public class OrganisationNumberSeLogger {

    private OrganisationNumberSeLogger() {
        throw new IllegalStateException("Utility class");
    }

    public static void logIfUnknownOrgnumber(String organisationNumber) {
        if (isUnknownOrgNumber(organisationNumber)) {
            log.info(
                    "Unknown organisation number with start digit: {}",
                    organisationNumber.charAt(0));
        }
    }

    public static void logIfUnknownOrgnumberForSuccessfulLogin(String organisationNumber) {
        if (isUnknownOrgNumber(organisationNumber)) {
            log.info(
                    "Successful login for unknown organisation number with start digit: {}",
                    organisationNumber.charAt(0));
        }
    }

    /**
     * Check if organisation number is unknown. Distinguish organisation number from SSN by checking
     * the third digit, which is always >= 2 for organisation numbers.
     *
     * @param organisationNumber
     * @return true if organisation number doesn't start with 5 and third digit is >= 2
     */
    private static boolean isUnknownOrgNumber(String organisationNumber) {
        if (Strings.isNullOrEmpty(organisationNumber)) {
            return false;
        }

        int thirdDigit;
        try {
            thirdDigit = Integer.parseInt(organisationNumber.substring(2, 3));
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            log.error("Error when parsing org number digit", e);
            return false;
        }

        return !organisationNumber.startsWith("5") && thirdDigit >= 2;
    }
}
