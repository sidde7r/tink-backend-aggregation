package se.tink.libraries.validation.validators;

import com.google.common.base.Strings;
import java.util.regex.Pattern;
import se.tink.libraries.validation.exceptions.InvalidEmailException;

public class EmailValidator {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile(
                    "^[_a-z0-9-+]+(\\.[_a-z0-9-+]+)*@[a-z0-9-]+(\\.[a-z0-9]+)*(\\.[a-z]{2,})$");

    public static void validate(String email) throws InvalidEmailException {
        if (Strings.isNullOrEmpty(email)) {
            throw new InvalidEmailException("Email must not be null or empty.");
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidEmailException("Email is not valid.");
        }
    }

    public static boolean isValid(String email) {
        try {
            validate(email);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
