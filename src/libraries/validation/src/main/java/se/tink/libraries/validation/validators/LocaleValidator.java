package se.tink.libraries.validation.validators;

import com.google.common.base.Strings;
import java.util.regex.Pattern;
import se.tink.libraries.validation.exceptions.InvalidLocaleException;

public class LocaleValidator {
    private static final Pattern LOCALE_PATTERN = Pattern.compile("[a-zA-Z]{2}_[a-zA-Z]{2}");

    public static void validate(String locale) throws InvalidLocaleException {
        if (Strings.isNullOrEmpty(locale)) {
            throw new InvalidLocaleException("Locale must not be null or empty.");
        }

        if (!LOCALE_PATTERN.matcher(locale).matches()) {
            throw new InvalidLocaleException("Locale is not valid.");
        }
    }

    public static boolean isValid(String locale) {
        try {
            validate(locale);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
