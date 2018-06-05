package se.tink.backend.utils.guavaimpl;

import com.google.common.base.Predicate;
import java.util.Objects;
import se.tink.libraries.application.GenericApplicationFieldGroup;

public class Predicates {
    public static Predicate<String> containsCaseInsensitive(final String string) {
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

    public static Predicate<String> startsWith(final String s) {
        return input -> input.startsWith(s);
    }

    public static Predicate<GenericApplicationFieldGroup> fieldGroupByName(final String fieldGroupName) {
        return fieldGroup -> Objects.equals(fieldGroup.getName(), fieldGroupName);
    }
}
