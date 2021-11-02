package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.utils;

import static se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.LogTags.UTILS_SPLIT_GET_PAGINATION_KEY;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.LoginParameter.USER_VALUE_PREFIX;

import io.vavr.collection.List;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.net.URISyntaxException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.QueryKeys;
import se.tink.libraries.identitydata.countries.EsIdentityDocumentType;

@Slf4j
public class BbvaUtils {

    private static final String ONLY_DIGITS_PATTERN = "[0-9]+";
    private static final String VALID_USERNAME_PATTERN = "[a-zA-Z0-9]{1,16}";
    private static final String RELAXED_DNI_PATTERN = "[0-9]{1,8}[A-Z]";
    private static final String RELAXED_NIE_PATTERN = "[XYZ][0-9]{1,7}[A-Z]";
    private static final int MAX_LEN_DNI = 10;
    private static final int USER_LENGTH_9 = 9;
    private static final int USER_LENGTH_7 = 7;
    private static final int USER_LENGTH_16 = 16;
    private static final String NINE_DIGITS_PREFIX = "404134";

    /**
     * Splits a URI string and gets the pagination key
     *
     * @param uriToSplit
     * @return
     */
    public static Option<String> splitGetPaginationKey(String uriToSplit) {
        return Try.of(() -> new URIBuilder(uriToSplit))
                .onFailure(
                        URISyntaxException.class,
                        e ->
                                log.error(
                                        "{}: Could not parse next page key in: {}",
                                        UTILS_SPLIT_GET_PAGINATION_KEY,
                                        uriToSplit,
                                        e))
                .map(URIBuilder::getQueryParams)
                .map(List::ofAll)
                .getOrElse(List::empty)
                .filter(p -> QueryKeys.PAGINATION_OFFSET.equals(p.getName()))
                .flatMap(p -> Option.of(p.getValue()))
                .headOption()
                .onEmpty(
                        () ->
                                log.warn(
                                        "{}: Trying to get next pagination key when none exists",
                                        UTILS_SPLIT_GET_PAGINATION_KEY));
    }

    /**
     * true if username seems a Spanish DNI (National Identification Number)
     *
     * @param username
     * @return
     */
    private static boolean seemsDNI(String username) {
        return username != null && username.matches(RELAXED_DNI_PATTERN);
    }

    /**
     * insert zeros at the beginning of the username until reach 10 characters
     *
     * @param username
     * @return
     */
    private static final String fillLeadingZerosDniUpTo10(String username) {
        return fillLeadingZerosDNI(username, MAX_LEN_DNI);
    }

    /**
     * insert zeros at the beginning of the username until reach 9 characters
     *
     * @param username
     * @return
     */
    private static final String fillLeadingZerosDNI(String username) {
        return fillLeadingZerosDNI(username, USER_LENGTH_9);
    }

    private static final String fillLeadingZerosDNI(String username, int len) {
        if (username != null && username.length() < len) {
            StringBuilder usernameBuilder = new StringBuilder(username);
            while (usernameBuilder.length() < len) {
                usernameBuilder.insert(0, "0");
            }
            username = usernameBuilder.toString();
        }
        return username;
    }

    /**
     * Validate the generic username format
     *
     * @param username
     * @return
     */
    private static boolean isValidUsername(String username) {
        return username != null && username.matches(VALID_USERNAME_PATTERN);
    }

    /**
     * Validate a Spanish DNI (National Identification Number)
     *
     * @param username
     * @return
     */
    private static boolean isValidDNI(String username) {
        return seemsDNI(username)
                && EsIdentityDocumentType.isValidNif(fillLeadingZerosDNI(username));
    }

    /**
     * true if username seems a Spanish NIE (Foreign Identification Number)
     *
     * @param username
     * @return
     */
    private static boolean seemsNIE(String username) {
        return username != null && username.matches(RELAXED_NIE_PATTERN);
    }

    /**
     * Validate a Spanish NIE (Foreign Identification Number)
     *
     * @param username
     * @return
     */
    private static boolean isValidNIE(String username) {
        return seemsNIE(username)
                && EsIdentityDocumentType.isValidNie(
                        username.substring(0, 1) + fillLeadingZerosDNI(username.substring(1), 8));
    }

    /**
     * Process 9 digits username Decompiled from the Android APP
     *
     * @param username
     * @return
     */
    private static String process9digitsUser(String username) {
        StringBuilder bbvaUsernameSB = new StringBuilder(NINE_DIGITS_PREFIX);
        bbvaUsernameSB.append(username);
        String bbvaUsername = bbvaUsernameSB.toString();
        int bbvaUsernameLen = bbvaUsername.length();
        int length = 0;
        int lenghtValidationResult = 0;
        int intResult = 0;
        int optionalResult = 0;
        while (length < bbvaUsernameLen) {
            int lenthPlusOne = length + 1;
            int digitValue = Integer.parseInt(bbvaUsername.substring(length, lenthPlusOne));
            if (length % 2 != 0) {
                lenghtValidationResult += digitValue;
            } else {
                digitValue *= 2;
                if (digitValue > USER_LENGTH_9) {
                    intResult += digitValue / 10 + digitValue % 10;
                } else {
                    optionalResult += digitValue;
                }
            }

            length = lenthPlusOne;
        }

        lenghtValidationResult =
                (lenghtValidationResult + intResult + optionalResult) % MAX_LEN_DNI;
        if (lenghtValidationResult > 0) {
            lenghtValidationResult = 10 - lenghtValidationResult;
        }

        bbvaUsernameSB.append(lenghtValidationResult);
        return bbvaUsernameSB.toString();
    }

    /**
     * if the username is a valid Spanish DNI then insert zeros at the beginning of the username
     * until reach 10 characters
     *
     * @param username
     * @return
     */
    private static String processPossibleDNI(String username) {
        if (username.length() <= MAX_LEN_DNI) {
            if (isValidDNI(username)) {
                return USER_VALUE_PREFIX.concat(fillLeadingZerosDniUpTo10(username));
            } else {
                return null;
            }
        } else {
            return USER_VALUE_PREFIX.concat(username);
        }
    }

    /**
     * Process a possible NIE
     *
     * @param username
     * @return
     */
    private static String processPossibleNIE(String username) {
        if (isValidNIE(username)) {
            return USER_VALUE_PREFIX.concat(String.valueOf(username));
        } else {
            return null;
        }
    }

    /**
     * According to the code extracted from the BBVA App (Android), code that is in the Eurobits
     * connector, the logic for processing the username is as follows:
     *
     * <p>First validation: Only ASCII alphanumeric characters with a maximum length of 16 and
     * minimum of 1 are accepted.
     *
     * <p>If correct, it is converted to upper case to continue the validation process.
     *
     * <p>If incorrect, null is returned.
     *
     * <p>If the username seems to be a Spanish DNI, then the DNI is validated.
     *
     * <p>If the validation of the DNI is correct, zeros (0) are inserted in front of the username,
     * up to a length of 10. The prefix "0019-" is returned plus the result of the username with
     * zeros inserted.
     *
     * <p>If the validation of the DNI is wrong, null is returned, meaning that it is a wrong
     * document.
     *
     * <p>If the username is only digits and its length is 9, the prefix "404134" plus the username
     * is returned. This seems to be a special case for BBVA internal users
     *
     * <p>If the username is only digits and its length is 7 or 16, the username is returned without
     * any prefix.
     *
     * <p>If the username seems to be a Spanish NIE, then the NIE is validated.
     *
     * <p>If the validation of the NIE is correct, the prefix "0019-" plus the username (NIE) is
     * returned.
     *
     * <p>If the validation of the NIE is wrong, null is returned, meaning that it is a wrong
     * document.
     *
     * <p>If it does not comply with the above validations, the prefix "0019-" plus the username is
     * returned.
     *
     * @param username
     * @return
     */
    public static String formatUser(String username) {
        if (isValidUsername(username)) {
            username = username.trim().toUpperCase();
            boolean onlyDigits = username.matches(ONLY_DIGITS_PATTERN);
            int usernameLength = username.length();
            if (seemsDNI(username)) { // possible DNI
                return processPossibleDNI(username);
            } else if (onlyDigits && usernameLength == USER_LENGTH_9) {
                return process9digitsUser(username);
            } else if (onlyDigits
                    && (usernameLength == USER_LENGTH_7 || usernameLength == USER_LENGTH_16)) {
                return username;
            } else if (seemsNIE(username)) { // Possible NIE
                return processPossibleNIE(username);
            } else {
                return USER_VALUE_PREFIX.concat(username);
            }
        }
        return null;
    }
}
