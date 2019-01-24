package se.tink.backend.aggregation.utils;

import java.util.Objects;
import java.util.function.Predicate;

public class Predicates {
    static Predicate<String> containsCaseInsensitive(final String string) {
        final String stringLowerCase = string != null ? string.toLowerCase() : null;

        return value -> {
            if (value == null && string == null) {
                return true;
            } else if (value == null || string == null) {
                return false;
            }

            return Objects.equals(value, string) || Objects.equals(value.toLowerCase(), stringLowerCase);
        };
    }
}
