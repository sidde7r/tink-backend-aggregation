package se.tink.libraries.phonenumbers.utils;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import se.tink.libraries.phonenumbers.InvalidPhoneNumberException;

public class PhoneNumberUtils {
    private static final PhoneNumberUtil PHONE_NUMBER_UTIL = PhoneNumberUtil.getInstance();
    private static final PhoneNumberUtil.PhoneNumberFormat NORMALIZE_FORMAT =
            PhoneNumberUtil.PhoneNumberFormat.E164;
    private static final String DEFAULT_COUNTRY = "";

    /**
     * Returns true or false if the phone number is valid. The number needs to include country
     * information.
     */
    public static boolean isValid(String phoneNumber) {
        try {
            normalize(phoneNumber);
            return true;
        } catch (InvalidPhoneNumberException e) {
            return false;
        }
    }

    /**
     * Returns the string representation of a phone number on E164 format (the format that is
     * recommended for storage).
     */
    public static String normalize(String phoneNumber) throws InvalidPhoneNumberException {
        try {
            Phonenumber.PhoneNumber result = PHONE_NUMBER_UTIL.parse(phoneNumber, DEFAULT_COUNTRY);

            if (PHONE_NUMBER_UTIL.isValidNumber(result)) {
                return PHONE_NUMBER_UTIL.format(result, NORMALIZE_FORMAT);
            }

            throw new InvalidPhoneNumberException(phoneNumber);
        } catch (NumberParseException e) {
            throw new InvalidPhoneNumberException(phoneNumber);
        }
    }
}
