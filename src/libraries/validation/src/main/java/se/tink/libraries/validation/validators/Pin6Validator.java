package se.tink.libraries.validation.validators;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import se.tink.libraries.validation.exceptions.InvalidPin6Exception;

/**
 * This validator validates that the PIN6 code is of correct format and meet the complexity
 * requirements. - We do not allow sequential number series - We do not allow repeating a number
 * more than 3 times.
 */
public class Pin6Validator {
    private static final Pattern LENGHT_PATTERN =
            Pattern.compile("(\\d){6}", Pattern.CASE_INSENSITIVE);
    private static final Integer MAX_OCCURRING_DIGIT_COUNT = 3;
    private static final ImmutableSet<String> SEQUENTIAL_NUMBER_SERIES =
            ImmutableSet.of(
                    "012345", "123456", "234567", "345678", "456789", "987654", "876543", "765432",
                    "654321", "543210");

    public static void validate(String pin6) throws InvalidPin6Exception {
        if (Strings.isNullOrEmpty(pin6)) {
            throw new InvalidPin6Exception("Pin code must not be null or empty.");
        }

        if (!LENGHT_PATTERN.matcher(pin6).matches()) {
            throw new InvalidPin6Exception("Pin code must be 6 digit long.");
        }

        if (SEQUENTIAL_NUMBER_SERIES.contains(pin6)) {
            throw new InvalidPin6Exception("Pin code must not be a series of sequential digits.");
        }

        if (countMaxOccurrences(pin6) > MAX_OCCURRING_DIGIT_COUNT) {
            throw new InvalidPin6Exception(
                    "Pin code must not contain the same digit more than three times.");
        }
    }

    public static void validateChange(String oldPin6, String newPin6) throws InvalidPin6Exception {
        validate(oldPin6);
        validate(newPin6);
        if (oldPin6.equals(newPin6)) {
            throw new InvalidPin6Exception("Old and new pin6 must not be the same.");
        }
        ;
    }

    private static Long countMaxOccurrences(String input) {
        return input.chars().boxed()
                .collect(Collectors.groupingBy(i -> ((char) i.intValue()), Collectors.counting()))
                .values().stream()
                .max(Long::compareTo)
                .orElse(0L);
    }
}
